import cz.zcu.kiv.DataTransformation.OffLineDataProvider;
import cz.zcu.kiv.FeatureExtraction.IFeatureExtraction;
import cz.zcu.kiv.FeatureExtraction.WaveletTransform;
import cz.zcu.kiv.Utils.SparkInitializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.junit.*;

import java.io.IOException;
import java.util.List;

import static cz.zcu.kiv.Utils.Const.REMOTE_TEST_DATA_DIRECTORY;
import static cz.zcu.kiv.Utils.Const.TRAINING_FILE;

/***********************************************************************************************************************
 *
 * This file is part of the Spark_EEG_Analysis project

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
 * FeatureExtractionTest, 2017/06/11 16:00 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class FeatureExtractionTest {

    private static Log logger = LogFactory.getLog(FeatureExtractionTest.class);

    @Before
    public void initalizeHDFSTest() throws IOException {
        EEGTest.initalizeHDFSTest();
    }

    @After
    public void unintializeHDFSTest() throws IOException {
        EEGTest.unintializeHDFSTest();
    }

    /*
    we will do a wavelet transform with following specifications:
        - 512 ms interval starting 175 ms after the beginning of each epoch is used
        - 5-level DWT using Daubechies 8 mother wavelet is performed
        - based on the results, 16 approximation coefficients of level 5 were used from each EEG channel
        - finally, all three results of DVVT were concatenated to form a feature vector of dimension 48.
    */
    private static Function<double[][], double[]> mapFunc = new Function<double[][], double[]>() {
        public double[] call(double[][] epoch) {
            IFeatureExtraction fe = new WaveletTransform(8, 512,175,16);
            return fe.extractFeatures(epoch);
        }
    };

    @Test
    public void test(){
        try{
            String[] files = {REMOTE_TEST_DATA_DIRECTORY+TRAINING_FILE};
            OffLineDataProvider odp =
                    new OffLineDataProvider(files);
            odp.loadData();
            List<double[][]> rawEpochs = odp.getData();
            List<Double> rawTargets = odp.getDataLabels();
            JavaRDD<double[][]> epochs = SparkInitializer.getJavaSparkContext().parallelize(rawEpochs);
            JavaRDD<Double> targets = SparkInitializer.getJavaSparkContext().parallelize(rawTargets);

            // a naive and ugly collect
            List<double[]> features = epochs.map(mapFunc).collect();

            logger.info("Dimensions of resulting epochs are: " + features.size() + " x " + features.get(0).length);

            //tests
            assert features.size() == 11;
            for (int i = 0; i < features.size(); i++){
                assert features.get(i).length == 48;
            }


            double allFeatures = 0;
            double epochSum = 0;

            for (double[] epoch : features){
                epochSum = 0;
                for (double epoc : epoch){
                    epochSum += epoc;
                }
                //System.out.println(epochSum);
                allFeatures += epochSum;
            }
            //System.out.println("Sum of all features" + allFeatures);
            assert allFeatures == -24.861844096031625;

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
