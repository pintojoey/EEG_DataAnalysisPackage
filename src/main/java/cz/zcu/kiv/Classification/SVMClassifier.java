package cz.zcu.kiv.Classification;

import cz.zcu.kiv.FeatureExtraction.IFeatureExtraction;
import cz.zcu.kiv.Utils.ClassificationStatistics;
import cz.zcu.kiv.Utils.SparkInitializer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.classification.LogisticRegressionWithSGD;
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

/***********************************************************************************************************************
 *
 * This file is part of the Spark_EEG_Analysis project

 * ==========================================
 *
 * Copyright (C) 2017 by University of West Bohemia (http://www.zcu.cz/en/)
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
public class SVMClassifier implements IClassifier {

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
}
