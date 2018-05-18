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
    public void worklflow_initializer_test() throws IOException {
        Workflow.initializeBlocks();

    }

    @Test
    public void workflow_parse() throws Exception{
            Workflow.initializeBlocks();
            String import_json = FileUtils.readFileToString(new File(Workflow.WORKFLOW_DESIGNER_DIRECTORY+"export.json"));
            JSONObject jobject = new JSONObject(import_json);
            Workflow.execute(jobject);

    }
}
