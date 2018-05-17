package cz.zcu.kiv.WorkflowDesigner;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
