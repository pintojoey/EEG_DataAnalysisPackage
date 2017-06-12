import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/***********************************************************************************************************************
 *
 * This file is part of the Spark_EEG_Analysis project

 * ==========================================
 *
 * Copyright (C) 2017 by University of West Bohemia (http://www.zcu.cz/en/)
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

public class TestRunner {
    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(HadoopLoadingTest.class);
        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }
        Result result2 = JUnitCore.runClasses(OfflineDataProviderTest.class);
        for (Failure failure : result2.getFailures()) {
            System.out.println(failure.toString());
        }

    }
}