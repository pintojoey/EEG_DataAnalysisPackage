package cz.zcu.kiv.WorkflowDesigner;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Workflow {
    public static String BLOCKS_DIRECTORY="Block/";
    public static String WORKFLOW_BLOCKS_FILE="workflow_blocks.js";
    public static void initializeBlocks(ArrayList<Block> blocks){
        new File(BLOCKS_DIRECTORY).mkdir();
        for(Block block:blocks){
            String filename=BLOCKS_DIRECTORY+block.getFamily()+File.separator+block.getName();
            try {
                FileUtils.writeStringToFile(new File(filename),block.toJS());
                String include="include('"+block.getFamily()+File.separator+block.getName()+".js');";
                FileUtils.writeStringToFile(new File(BLOCKS_DIRECTORY+WORKFLOW_BLOCKS_FILE),include,true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
