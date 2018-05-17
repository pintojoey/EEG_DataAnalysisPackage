import cz.zcu.kiv.Classification.*;
import cz.zcu.kiv.DataTransformation.OffLineDataProvider;
import cz.zcu.kiv.FeatureExtraction.IFeatureExtraction;
import cz.zcu.kiv.FeatureExtraction.WaveletTransform;
import cz.zcu.kiv.Pipeline.PipelineBuilder;
import cz.zcu.kiv.Utils.ClassificationStatistics;
import cz.zcu.kiv.WorkflowDesigner.Block;
import cz.zcu.kiv.WorkflowDesigner.Workflow;
import cz.zcu.kiv.WorkflowDesigner.WorkflowLogic;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/***********************************************************************************************************************
 *
 * This file is part of the EEG_Analysis project

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
 * WorkflowDesignerTest, 2018/17/05 6:32 Joey Pinto
 *
 * This test verifies the creation of all available blocks in the designer
 **********************************************************************************************************************/
public class WorkflowDesignerTest {
    private static Log logger = LogFactory.getLog(PipelineBuilder.class);

//
//    public void execute() throws Exception {
//        String[] files = new String[5];
//
//        /*
//           1. get the input
//           a) within a info.txt file
//           b) a .eeg file reference and the target number
//         */
//        if (queryMap.containsKey("info_file")){
//            files[0]=queryMap.get("info_file");
//        }
//        else if (queryMap.containsKey("eeg_file") && queryMap.containsKey("guessed_num") ){
//            files[0] = queryMap.get("eeg_file");
//            files[1] = queryMap.get("guessed_num");
//        }
//        else {
//            throw new IllegalArgumentException("Missing the input file argument");
//        }
//
//        OffLineDataProvider odp =
//                new OffLineDataProvider(files);
//
//        odp.loadData();
//        List<double[][]> rawEpochs = odp.getData();
//        List<Double> rawTargets = odp.getDataLabels();
//
//        /*
//            2. set the feature extraction
//         */
//        // @ feature extraction parameter
//
//        IFeatureExtraction fe;
//        if (queryMap.containsKey("fe")){
//            String type = queryMap.get("fe");
//            if(type.equals("dwt-8")){
//                fe =  new WaveletTransform(8, 512, 175, 16);
//            }
//            else{
//                throw new IllegalArgumentException("Unsupported feature extraction argument");
//            }
//        }
//        else{
//            throw new IllegalArgumentException("Missing the feature extraction argument");
//        }
//
//        /*
//            3. set the classifier, either:
//                a) load a classifier
//                b) train a new one
//            also there is the option to save a classifier
//         */
//        // @ classifier parameter
//        IClassifier classifier;
//        ClassificationStatistics classificationStatistics;
//
//        if(queryMap.containsKey("train_clf")){
//
//            String classifierType = queryMap.get("train_clf");
//            logger.info("1. Training a classifier of type " + classifierType);
//
//            switch (classifierType){
//                case "svm": classifier = new SVMClassifier();
//                    break;
//                case "logreg": classifier = new LogisticRegressionClassifier();
//                    break;
//                case "dt": classifier = new DecisionTreeClassifier();
//                    break;
//                case "rf" : classifier = new RandomForestClassifier();
//                    break;
//                case "nn" : classifier = new NeuralNetworkClassifier();
//                    break;
//                default: classifier = null;
//                    throw new IllegalArgumentException("Unsupported classifier argument");
//            }
//            logger.info("2. Getting the required data");
//            // total data
//            List<double[][]> data = odp.getData();
//            List<Double> targets = odp.getDataLabels();
//
//            logger.info("2. -> loaded the required data");
//
//            // shuffle the data but use the same seed !
//            long seed = 1;
//            Collections.shuffle(data,new Random(seed));
//            Collections.shuffle(targets,new Random(seed));
//
//            // training data
//            List<double[][]> trainEpochs = data.subList(0,(int)(data.size()*0.7));
//            List<Double> trainTargets = targets.subList(0,(int)(targets.size()*0.7));
//
//            // testing data
//            List<double[][]> testEpochs = data.subList((int)(data.size()*0.7),data.size());
//            List<Double> testTargets = targets.subList((int)(targets.size()*0.7),targets.size());
//            logger.info("Loaded the data into memory");
//
//            // Load config
//            HashMap<String,String> config = new HashMap<>(10);
//            for (String key : queryMap.keySet()){
//                if(key.startsWith("config_")){
//                    config.put(key,queryMap.get(key));
//                }
//            }
//
//            // into the classifier
//            classifier.setConfig(config);
//
//            logger.info("Set the classifier config");
//
//            // train
//            classifier.train(trainEpochs, trainTargets, fe);
//
//            logger.info("Trained the model");
//
//            if(queryMap.containsKey("save_clf")){
//                if(queryMap.get("save_clf").equals("true")){
//                    logger.info("Saving classifier");
//                    if(queryMap.containsKey("save_name")){
//                        classifier.save(queryMap.get("save_name"));
//                    }
//                    else{
//                        throw new IllegalArgumentException("Please provide a location to save a classifier within the  save_location query parameter");
//                    }
//                }
//            }
//            logger.info("Saved classifier");
//
//            //test
//            classificationStatistics = classifier.test(testEpochs,testTargets);
//
//            logger.info("Got the classification statistics");
//
//        }
//        else if(queryMap.containsKey("load_clf")){
//
//            logger.info("Loading a saved classifier");
//
//            // get the type of classifier
//            String classifierType = queryMap.get("load_clf");
//            // get the location of the classifier
//            String classifierPath;
//            if(queryMap.containsKey("load_name")){
//                classifierPath = queryMap.get("load_name");
//            }
//            else{
//                throw new IllegalArgumentException("Classifier location not provided");
//            }
//
//            switch (classifierType){
//                case "svm": classifier = new SVMClassifier();
//                    break;
//                case "logreg": classifier = new LogisticRegressionClassifier();
//                    break;
//                case "dt": classifier = new DecisionTreeClassifier();
//                    break;
//                case "rf" : classifier = new RandomForestClassifier();
//                    break;
//                case "nn" : classifier = new NeuralNetworkClassifier();
//                    break;
//                default: classifier = null;
//                    throw new IllegalArgumentException("Unsupported classifier argument");
//            }
//            logger.info("Classifier type is " + classifierType);
//            logger.info("Classifier path is " + classifierPath);
//
//
//            // total data
//            List<double[][]> data = odp.getData();
//            List<Double> targets = odp.getDataLabels();
//
//            // shuffle the data but use the same seed !
//            long seed = 1;
//            Collections.shuffle(data,new Random(seed));
//            Collections.shuffle(targets,new Random(seed));
//            logger.info("Loaded the data into memory");
//
//            // train
//            classifier.setFeatureExtraction(fe);
//            classifier.load(classifierPath);
//
//            logger.info("Loaded the classifier");
//
//            //test
//            classificationStatistics = classifier.test(data,targets);
//            logger.info("Produced the classification statistics");
//
//        }
//        else{
//            throw new IllegalArgumentException("Missing classifier argument");
//        }
//
//        logger.info(classificationStatistics);
//
//        if(queryMap.containsKey("result_path")){
//            File file = new File(queryMap.get("result_path"));
//            PrintWriter printWriter = new PrintWriter (file);
//            printWriter.println (classificationStatistics);
//            printWriter.close ();
//        }
//
//    }



    @Test
    public void worklflow_initializer_test(){
/*
        Reflections reflections = new Reflections("cz.zcu.kiv.DataTransformation");
        Set<Class<? extends WorkflowLogic >> classes = reflections.getSubTypesOf(WorkflowLogic.class);
        for(Class class_instance:classes){
            try {
                WorkflowLogic workflowLogic = (WorkflowLogic) class_instance.newInstance();
                workflowLogic.initialize();
                System.out.println(workflowLogic.getBlock().getName());
            } catch (InstantiationException | IllegalAccessException e) {
                System.err.println(class_instance.getName() + " must have public default empty constructor!");
                e.printStackTrace();
            }
        }
*/
        Block offline_data=new OffLineDataProvider().initialize();
        assert offline_data !=null;
        Block wavelet_transform=new WaveletTransform().initialize();
        assert wavelet_transform !=null;
        Block svm_classifier=new SVMClassifier().initialize();
        assert svm_classifier !=null;


    }

    @Test
    public void workflow_parse(){
        try {
            Workflow.initializeWorkflow();
            String import_json = FileUtils.readFileToString(new File(Workflow.WORKFLOW_DESIGNER_DIRECTORY+"export.json"));
            JSONObject jobject = new JSONObject(import_json);

            JSONArray blocks_array = jobject.getJSONArray("blocks");
            HashMap<Integer,Block>blocks=new HashMap<>();
            for(int i=0; i<blocks_array.length(); i++){
                JSONObject block_object=blocks_array.getJSONObject(i);
                Block block = new Block(block_object);
                blocks.put(block_object.getInt("id"),block);
            }

            JSONArray edges_array = jobject.getJSONArray("edges");
            boolean all_processed=false;
            while(!all_processed){
                all_processed=true;
                //Populate wait queue
                ArrayList<Integer>wait=new ArrayList<>();
                for(int i=0;i<edges_array.length();i++) {
                    JSONObject edge_object = edges_array.getJSONObject(i);
                    Block block1 = blocks.get(edge_object.getInt("block1"));
                    Block block2 = blocks.get(edge_object.getInt("block2"));
                    if (block1.isProcessed() && !block2.isProcessed()) {
                        wait.add(edge_object.getInt("block2"));
                    }
                    if(!block1.isProcessed()||!block2.isProcessed()){
                        all_processed=false;
                    }
                }

                //Process wait queue
                for(int j=0;j<wait.size();j++){
                    boolean ready = true;
                    int wait_block_id=wait.get(j);
                    Block wait_block=blocks.get(wait_block_id);

                    HashMap<Integer,Block> dependencies = new HashMap<>();
                    HashMap<String,String> source_param=new HashMap<>();
                    HashMap<String,Integer> source_block=new HashMap<>();

                    //Check dependencies of waiting block
                    for(int i=0;i<edges_array.length();i++) {
                        JSONObject edge_object = edges_array.getJSONObject(i);
                        if(wait_block_id!=edge_object.getInt("block2"))continue;

                        JSONArray connector1=edge_object.getJSONArray("connector1");
                        JSONArray connector2=edge_object.getJSONArray("connector2");
                        int block1_id=edge_object.getInt("block1");
                        Block block1 = blocks.get(block1_id);

                        for(int k=0;k<connector1.length();k++){
                            source_param.put(connector2.getString(k),connector1.getString(k));
                            source_block.put(connector2.getString(k),block1_id);
                        }

                        dependencies.put(block1_id,block1);

                        if(!block1.isProcessed()){
                            ready=false;
                            break;
                        }
                    }
                    if(ready){
                        //Process the ready block
                        System.out.println(dependencies.size());
                        wait_block.setProcessed(true);
                        break;
                    }

                }
            }
            System.out.println("Done!!");


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
