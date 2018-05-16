package cz.zcu.kiv.WorkflowDesginer;

import java.util.ArrayList;
/***********************************************************************************************************************
 *
 * This file is part of the EEG_Analysis project

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
 * Block, 2018/16/05 13:32 Joey
 *
 * This file is the model for a single block in the worklow designer tool
 **********************************************************************************************************************/


public class Block {
    private String name;
    private String family;
    private ArrayList<Data> input;
    private ArrayList<Data> output;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public ArrayList<Data> getInput() {
        return input;
    }

    public void setInput(ArrayList<Data> input) {
        this.input = input;
    }

    public ArrayList<Data> getOutput() {
        return output;
    }

    public void setOutput(ArrayList<Data> output) {
        this.output = output;
    }
}
