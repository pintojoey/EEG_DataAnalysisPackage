package cz.zcu.kiv.WorkflowDesginer;

import org.json.JSONObject;

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
 * Property, 2018/16/05 13:32 Joey
 *
 * This file is a model for the preoperties of the blocks in the workflow designer tool
 **********************************************************************************************************************/
public class Property {
    private String name;
    private String cardinality;
    private String type;
    private String defaultValue;
    private ArrayList<Object> values;
    private JSONObject schema;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCardinality() {
        return cardinality;
    }

    public void setCardinality(String cardinality) {
        this.cardinality = cardinality;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public ArrayList<Object> getValues() {
        return values;
    }

    public void setValues(ArrayList<Object> values) {
        this.values = values;
    }

    public JSONObject getSchema() {
        return schema;
    }

    public void setSchema(JSONObject schema) {
        this.schema = schema;
    }
}
