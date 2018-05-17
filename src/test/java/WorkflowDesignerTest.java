import cz.zcu.kiv.Classification.SVMClassifier;
import cz.zcu.kiv.DataTransformation.OffLineDataProvider;
import cz.zcu.kiv.FeatureExtraction.WaveletTransform;
import cz.zcu.kiv.WorkflowDesigner.Block;
import cz.zcu.kiv.WorkflowDesigner.Workflow;
import org.junit.Test;
import java.io.IOException;
import java.util.ArrayList;
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

    @Test
    public void worklflow_initializer_test(){

        Block offline_data=new OffLineDataProvider().initialize();
        assert offline_data !=null;
        Block wavelet_transform=new WaveletTransform().initialize();
        assert wavelet_transform !=null;
        Block svm_classifier=new SVMClassifier().initialize();
        assert svm_classifier !=null;

        ArrayList<Block>blocks=new ArrayList<>();
        blocks.add(offline_data);
        blocks.add(wavelet_transform);
        blocks.add(svm_classifier);
        try {
            Workflow.initializeBlocks(blocks);
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }

    }
}
