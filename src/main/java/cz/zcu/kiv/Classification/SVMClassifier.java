package cz.zcu.kiv.Classification;

import cz.zcu.kiv.FeatureExtraction.IFeatureExtraction;
import cz.zcu.kiv.Utils.ClassificationStatistics;
import cz.zcu.kiv.Utils.SparkInitializer;
import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockType;
import cz.zcu.kiv.WorkflowDesigner.Block;
import cz.zcu.kiv.WorkflowDesigner.Data;
import cz.zcu.kiv.WorkflowDesigner.Property;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.classification.SVMModel;
import org.apache.spark.mllib.classification.SVMWithSGD;
import org.apache.spark.mllib.evaluation.MulticlassMetrics;
import org.apache.spark.mllib.linalg.DenseVector;
import org.apache.spark.mllib.regression.LabeledPoint;
import scala.Tuple2;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static cz.zcu.kiv.WorkflowDesigner.DataField.*;
import static cz.zcu.kiv.WorkflowDesigner.DataType.*;
import static cz.zcu.kiv.WorkflowDesigner.Field.*;
import static cz.zcu.kiv.WorkflowDesigner.Field.FEATURE_SIZE_FIELD;
import static cz.zcu.kiv.WorkflowDesigner.Type.NUMBER;
import static cz.zcu.kiv.WorkflowDesigner.WorkflowBlock.SVM_CLASSIFIER;
import static cz.zcu.kiv.WorkflowDesigner.WorkflowCardinality.ONE_TO_MANY;
import static cz.zcu.kiv.WorkflowDesigner.WorkflowCardinality.ONE_TO_ONE;
import static cz.zcu.kiv.WorkflowDesigner.WorkflowFamily.MACHINE_LEARNING;

/***********************************************************************************************************************
 *
 * This file is part of the Spark_EEG_Analysis project

 * ==========================================
 *
 * Copyright (C) 2018 by University of West Bohemia (http://www.zcu.cz/en/)
 *
 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************
 *
 * SVMClassifier, 2017/06/27 12:13 Dorian Beganovic
 *
 **********************************************************************************************************************/
@BlockType(type = SVM_CLASSIFIER, family = MACHINE_LEARNING)
public class SVMClassifier extends Block implements IClassifier {

    private static Log logger = LogFactory.getLog(SVMClassifier.class);
    private static IFeatureExtraction fe;
    private static SVMModel model;
    private HashMap<String,String> config;

    private static Function<double[][], double[]> featureExtractionFunc = new Function<double[][], double[]>() {
        public double[] call(double[][] epoch) {
            return fe.extractFeatures(epoch);
        }
    };

    private static Function<Tuple2<Double, double[]>, LabeledPoint> unPackFunction = new Function<Tuple2<Double, double[]>, LabeledPoint>() {
        @Override
        public LabeledPoint call(Tuple2<Double, double[]> v1) throws Exception {
            return new LabeledPoint(v1._1(),new DenseVector(v1._2()));
        }
    };

    private static Function<LabeledPoint, Tuple2<Object, Object>> classifyFunction = new Function<LabeledPoint, Tuple2<Object, Object>>() {
        public Tuple2<Object, Object> call(LabeledPoint p) {
            Double prediction = model.predict(p.features());
            return new Tuple2<Object, Object>(prediction, p.label());
        }
    };


    @Override
    public void setFeatureExtraction(IFeatureExtraction fe) {
        SVMClassifier.fe = fe;
    }

    @Override
    public void train(List<double[][]> epochs, List<Double> targets, IFeatureExtraction fe) {
        SVMClassifier.fe = fe;
        JavaRDD<double[][]> rddEpochs = SparkInitializer.getJavaSparkContext().parallelize(epochs);
        JavaRDD<Double> rddTargets = SparkInitializer.getJavaSparkContext().parallelize(targets);

        JavaRDD<double[]> features = rddEpochs.map(featureExtractionFunc);

        JavaPairRDD<Double, double[]> rawData = rddTargets.zip(features);

        JavaRDD<LabeledPoint> training = rawData.map(unPackFunction);

        // Run training algorithm to build the model
        if(config.containsKey("config_num_iterations") && config.containsKey("config_step_size") &&
        config.containsKey("config_reg_param") && config.containsKey("config_mini_batch_fraction")){

            logger.info("Creating the model with configuration");
            SVMClassifier.model = new SVMWithSGD().train(
                    training.rdd(),
                    Integer.parseInt(config.get("config_num_iterations")),
                    Double.parseDouble(config.get("config_step_size")),
                    Double.parseDouble(config.get("config_reg_param")),
                    Double.parseDouble(config.get("config_mini_batch_fraction"))
            );
        }
        else {
            logger.info("Creating the model without configuration");
            SVMClassifier.model = new SVMWithSGD().run(training.rdd());
        }
    }

    @Override
    public ClassificationStatistics test(List<double[][]> epochs, List<Double> targets) {

        if(model==null){
            throw new IllegalStateException("The classifier has not been trained");
        }

        JavaRDD<double[][]> rddEpochs = SparkInitializer.getJavaSparkContext().parallelize(epochs);
        JavaRDD<Double> rddTargets = SparkInitializer.getJavaSparkContext().parallelize(targets);
        JavaRDD<double[]> features = rddEpochs.map(featureExtractionFunc);
        JavaPairRDD<Double, double[]> rawData = rddTargets.zip(features);
        JavaRDD<LabeledPoint> test = rawData.map(unPackFunction);
        JavaRDD<Tuple2<Object, Object>> predictionAndLabels = test.map(classifyFunction);
        // Get evaluation metrics
        MulticlassMetrics metrics = new MulticlassMetrics(predictionAndLabels.rdd());

        double[] confusionMatrix = metrics.confusionMatrix().toArray();
        int tn = (int) confusionMatrix[0];
        int fp = (int) confusionMatrix[1];
        int fn = (int) confusionMatrix[2];
        int tp = (int) confusionMatrix[3];
        ClassificationStatistics statistics = new ClassificationStatistics(tp,tn,fp,fn);

        return statistics;
    }

    @Override
    public void save(String file) throws IOException {
        FileUtils.deleteDirectory(new File(file));
        model.save(SparkInitializer.getSparkContext(),file);
    }

    @Override
    public void load(String file) {
        model = SVMModel.load(SparkInitializer.getSparkContext(),file);
    }

    @Override
    public IFeatureExtraction getFeatureExtraction() {
        return fe;
    }

    @Override
    public void setConfig(HashMap<String, String> config) {
        this.config = config;
    }


    @Override
    public void initialize() {
        HashMap<String,Property>properties=new HashMap<>();

        properties.put(ITERATIONS_FIELD,new Property(ITERATIONS_FIELD, NUMBER, "name"));
        properties.put(STEP_SIZE,new Property(STEP_SIZE, NUMBER, "1"));
        properties.put(REG_PARAMETERS,new Property(REG_PARAMETERS, NUMBER, "0"));
        properties.put(MINI_BATCH_FRACTION,new Property(MINI_BATCH_FRACTION, NUMBER, "1"));

        final HashMap<String,Data>input=new HashMap<>();
        input.put(RAW_EPOCHS_OUTPUT,new Data(RAW_EPOCHS_OUTPUT,EPOCH_LIST, ONE_TO_ONE));
        input.put(RAW_TARGETS_OUTPUT,new Data(RAW_TARGETS_OUTPUT,TARGET_LIST, ONE_TO_ONE));
        input.put(FEATURE_EXTRACTOR_OUTPUT,new Data(FEATURE_EXTRACTOR_OUTPUT,FEATURE_EXTRACTOR, ONE_TO_ONE));

        final HashMap<String,Data>output=new HashMap<>();
        output.put(CLASSIFICATION_MODEL_OUTPUT, new Data(CLASSIFICATION_MODEL_OUTPUT,MODEL, ONE_TO_MANY));
        output.put(CLASSIFICATION_STATISTICS_OUTPUT, new Data(CLASSIFICATION_STATISTICS_OUTPUT,MODEL, ONE_TO_MANY));

        setInput(input);
        setOutput(output);
        setProperties(properties);
    }

    @Override
    public void process() {
        setFeatureExtraction((IFeatureExtraction) getInput().get(FEATURE_EXTRACTOR_OUTPUT).getValue());
        List<double[][]> epochs = (List<double[][]>) getInput().get(RAW_EPOCHS_OUTPUT).getValue();
        List<Double>targets = (List<Double>) getInput().get(RAW_TARGETS_OUTPUT).getValue();
        this.config=new HashMap<>();
        config.put("config_num_iterations",String.valueOf(getProperties().get(ITERATIONS_FIELD).asInt()));
        config.put("config_step_size",getProperties().get(STEP_SIZE).asString());
        config.put("config_reg_param",getProperties().get(REG_PARAMETERS).asString());
        config.put("config_mini_batch_fraction",getProperties().get(MINI_BATCH_FRACTION).asString());
        train(epochs, targets, getFeatureExtraction());

        getOutput().get(CLASSIFICATION_MODEL_OUTPUT).setValue(model);
    }




}
