package cz.zcu.kiv.WorkflowDesigner;

import org.apache.commons.io.FileUtils;

import java.io.File;
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
 * Workflow, 2018/17/05 6:32 Joey Pinto
 *
 * This file hosts the methods used to dynamically create the Javascript files needed for the workflow designer
 **********************************************************************************************************************/
public class Workflow {

    public static String WORKFLOW_DESIGNER_DIRECTORY ="workflow_designer/";
    public static String BLOCK_DEFINTION_DIRECTORY ="blocks/";
    public static String WORKFLOW_BLOCKS_FILE="workflow_blocks.js";

    public static void initializeBlocks(ArrayList<Block> blocks) throws IOException {
        String blocks_folder=WORKFLOW_DESIGNER_DIRECTORY +File.separator+BLOCK_DEFINTION_DIRECTORY;
        FileUtils.deleteDirectory(new File(blocks_folder));
        new File(blocks_folder).mkdirs();
        FileUtils.writeStringToFile(new File(WORKFLOW_DESIGNER_DIRECTORY +WORKFLOW_BLOCKS_FILE),
                "function include(file) {\n" +
                        "\t$('head').append('<script type=\"text/javascript\" src=\"'+file+'\"></script>');\n" +
                        "}\n");

        for(Block block:blocks){
            String filename= blocks_folder + block.getFamily()+File.separator+block.getName()+".js";


                FileUtils.writeStringToFile(new File(filename),block.toJS());
                String include="include('"+BLOCK_DEFINTION_DIRECTORY+block.getFamily()+File.separator+block.getName()+".js');";
                FileUtils.writeStringToFile(new File(WORKFLOW_DESIGNER_DIRECTORY +WORKFLOW_BLOCKS_FILE),include,true);
        }
    }
}
