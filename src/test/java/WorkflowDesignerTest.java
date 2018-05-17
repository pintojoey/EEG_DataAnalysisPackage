import cz.zcu.kiv.FeatureExtraction.WaveletTransform;
import cz.zcu.kiv.WorkflowDesigner.Block;
import cz.zcu.kiv.WorkflowDesigner.Workflow;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

public class WorkflowDesignerTest {

    @Test
    public void wavelet_transform_test(){
        Block block=new WaveletTransform().initialize();
        assert block !=null;
        ArrayList<Block>blocks=new ArrayList<>();
        blocks.add(block);
        try {
            Workflow.initializeBlocks(blocks);
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }

    }
}
