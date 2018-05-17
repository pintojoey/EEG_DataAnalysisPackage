import cz.zcu.kiv.FeatureExtraction.WaveletTransform;
import cz.zcu.kiv.WorkflowDesigner.Block;
import org.junit.Test;

public class WorkflowDesignerTest {

    @Test
    public void wavelet_transform_test(){
        Block block=new WaveletTransform().initialize();
        System.out.println(block.toJS());
        assert block !=null;

    }
}
