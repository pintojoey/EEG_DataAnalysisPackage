import cz.zcu.kiv.Classification.SVMClassifier;
import cz.zcu.kiv.DataTransformation.OffLineDataProvider;
import cz.zcu.kiv.FeatureExtraction.WaveletTransform;
import cz.zcu.kiv.WorkflowDesigner.Block;
import cz.zcu.kiv.WorkflowDesigner.Workflow;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

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
