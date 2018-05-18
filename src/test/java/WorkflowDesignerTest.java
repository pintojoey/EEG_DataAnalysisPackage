import cz.zcu.kiv.Classification.*;
import cz.zcu.kiv.DataTransformation.OffLineDataProvider;
import cz.zcu.kiv.FeatureExtraction.WaveletTransform;
import cz.zcu.kiv.Pipeline.PipelineBuilder;
import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockType;
import cz.zcu.kiv.WorkflowDesigner.Block;
import cz.zcu.kiv.WorkflowDesigner.WorkflowBlock;
import cz.zcu.kiv.WorkflowDesigner.Workflow;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static cz.zcu.kiv.WorkflowDesigner.WorkflowBlock.INFOTXT_FILE;
import static cz.zcu.kiv.WorkflowDesigner.WorkflowBlock.SVM_CLASSIFIER;
import static cz.zcu.kiv.WorkflowDesigner.WorkflowBlock.WAVELET_TRANSFORM;

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

    @Before
    public void initalizeHDFSTest() throws IOException {
        EEGTest.initalizeHDFSTest();
    }

    @After
    public void unintializeHDFSTest() throws IOException {
        EEGTest.unintializeHDFSTest();
    }


    @Test
    public void worklflow_initializer_test(){


        Set<Class<?>> block_types = new Reflections("cz.zcu.kiv").getTypesAnnotatedWith(BlockType.class);
        for(Class block_type:block_types){
            try {
                Block block= (Block) block_type.newInstance();
                assert block!=null;
                Annotation annotation = block_type.getAnnotation(BlockType.class);
                Class<? extends Annotation> type = annotation.annotationType();
                String block_type_name=(String)type.getDeclaredMethod("type").invoke(annotation, (Object[])null);
                String block_type_family=(String)type.getDeclaredMethod("family").invoke(annotation, (Object[])null);
                block.setName(block_type_name);
                block.setFamily(block_type_family);
                block.initialize();
                Workflow.block_definitions.add(block);


            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        Workflow.initializeWorkflow();


    }

    @Test
    public void workflow_parse() throws Exception {
        try {
            Workflow.initializeWorkflow();
            String import_json = FileUtils.readFileToString(new File(Workflow.WORKFLOW_DESIGNER_DIRECTORY+"export.json"));
            JSONObject jobject = new JSONObject(import_json);

            JSONArray blocks_array = jobject.getJSONArray("blocks");
            HashMap<Integer,Block>blocks=new HashMap<>();
            for(int i=0; i<blocks_array.length(); i++){
                JSONObject block_object=blocks_array.getJSONObject(i);

                Block block = null;

                Set<Class<?>> block_types = new Reflections("cz.zcu.kiv").getTypesAnnotatedWith(BlockType.class);
                for(Class block_type:block_types){
                    Annotation annotation = block_type.getAnnotation(BlockType.class);
                    Class<? extends Annotation> type = annotation.annotationType();
                    String block_type_name=(String)type.getDeclaredMethod("type").invoke(annotation, (Object[])null);
                    if (block_object.getString("type").equals(block_type_name)){
                        block = (Block) block_type.newInstance();
                    }
                }

                    block.initialize();
                    block.fromJSON(block_object);
                    blocks.put(block_object.getInt("id"),block);

            }

            JSONArray edges_array = jobject.getJSONArray("edges");
            Block wait_block;
            while(true){
                //Populate wait queue
                ArrayList<Integer>wait=new ArrayList<>();
                for(int i=0;i<edges_array.length();i++) {
                    JSONObject edge_object = edges_array.getJSONObject(i);
                    Block block1 = blocks.get(edge_object.getInt("block1"));
                    Block block2 = blocks.get(edge_object.getInt("block2"));
                    if(!block1.isProcessed()){
                        if(block1.getInput()==null||block1.getInput().size()==0){
                            wait.add(edge_object.getInt("block1"));
                        }
                    }
                    if(!block2.isProcessed()){
                        if (block1.isProcessed() && !block2.isProcessed()) {
                            wait.add(edge_object.getInt("block2"));
                        }
                    }


                }
                //Wait queue is empty, exit
                if(wait.size()==0)break;

                //Process wait queue
                for (Integer aWait : wait) {
                    boolean ready = true;
                    int wait_block_id = aWait;
                    System.out.println("Wait block " + wait_block_id);
                    wait_block = blocks.get(wait_block_id);

                    HashMap<Integer, Block> dependencies = new HashMap<>();
                    HashMap<String, String> source_param = new HashMap<>();
                    HashMap<String, Integer> source_block = new HashMap<>();

                    //Check dependencies of waiting block
                    for (int i = 0; i < edges_array.length(); i++) {
                        JSONObject edge_object = edges_array.getJSONObject(i);
                        if (wait_block_id != edge_object.getInt("block2")) continue;

                        JSONArray connector1 = edge_object.getJSONArray("connector1");
                        JSONArray connector2 = edge_object.getJSONArray("connector2");
                        int block1_id = edge_object.getInt("block1");
                        System.out.println("Dependent " + block1_id);
                        Block block1 = blocks.get(block1_id);

                        for (int k = 0; k < connector1.length(); k++) {
                            source_param.put(connector2.getString(k), connector1.getString(k));
                            source_block.put(connector2.getString(k), block1_id);
                        }

                        dependencies.put(block1_id, block1);

                        if (!block1.isProcessed()) {
                            ready = false;
                            break;
                        }
                    }
                    if (ready) {
                        //Process the ready block
                        System.out.println(dependencies.size());
                        System.out.println("Processing "+ wait_block_id);
                        wait_block.processBlock(dependencies, source_block, source_param);

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
