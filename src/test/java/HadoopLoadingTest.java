import cz.zcu.kiv.Utils.SparkInitializer;
import cz.zcu.kiv.signal.DataTransformer;
import cz.zcu.kiv.Utils.Const;
import cz.zcu.kiv.signal.HDFSEEGDataTransformer;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.*;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteOrder;
import java.util.ArrayList;

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
 * Baseline, 2017/05/25 22:05 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class HadoopLoadingTest {

    @Before
    public void initalizeHDFSTest() throws IOException {
        EEGTest.initalizeHDFSTest();
    }

    @After
    public void unintializeHDFSTest() throws IOException {
        EEGTest.unintializeHDFSTest();
    }

    @Test
    public void tryRAWEEG() {
        try {
            // instantiate the EEG data transformer
            DataTransformer transformer = new HDFSEEGDataTransformer(Const.HDFS_URI,Const.HDFS_CONF);

            // variables which are important to track
            String hdfsvhdrFileLocation;
            String hdfsEEGFileLocation;
            String outputFileLocation;
            int channel;

            /* analyze the input
             we expect 4 inputs:
             - 1. location of .vhdr file
             - 2. location of .eeg file
             - 3. location where you want the output file to be stored
             - 4. number of channels
            */
            hdfsvhdrFileLocation = Const.REMOTE_TEST_DATA_DIRECTORY+"/DoD/DoD2015_01.vhdr";
            hdfsEEGFileLocation = Const.REMOTE_TEST_DATA_DIRECTORY+"/DoD/DoD2015_01.eeg";
            outputFileLocation = Const.REMOTE_TEST_DATA_DIRECTORY+"/Dod";
            channel = 3; // kinda the default value
            //List<ChannelInfo> channels = transformer.getChannelInfo(args[0]);
            //int channel = Integer.parseInt(args[args.length - 1]);

            // these are the extracted values from the .eeg file
            double[] dataInValues;
            dataInValues = transformer.readBinaryData(hdfsvhdrFileLocation, hdfsEEGFileLocation, channel, ByteOrder.LITTLE_ENDIAN);

            // SPARK PART

            JavaSparkContext sc = SparkInitializer.getJavaSparkContext();


            // this is kinda ugly to do, but I don't think it's a big performance issue since
            // the arraylist doesn't have to regrow it should be an O(n) operation
            ArrayList<Double> dataIn = new ArrayList<>(dataInValues.length);
            for (double dataInValue : dataInValues) {
                dataIn.add(dataInValue);
            }
            JavaRDD<Double> rawVals = sc.parallelize(dataIn);

            // now write the output to FS

            FileSystem fs = FileSystem.get(URI.create(Const.HDFS_URI), Const.HDFS_CONF);

            if (fs.exists(new Path(outputFileLocation))){
                fs.delete(new Path(outputFileLocation),true);
            }
            // now save it using spark
            //System.out.println(fs.getUri());
            rawVals.saveAsTextFile(fs.getUri() + outputFileLocation);


            assert fs.exists(new Path(fs.getUri() + outputFileLocation)) == true;
//            for (EEGMarker marker: list) {
//                System.out.println(marker.getName() + " " + marker.getPosition());
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
