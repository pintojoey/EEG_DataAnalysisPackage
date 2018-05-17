package cz.zcu.kiv.WorkflowDesigner;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

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
    private HashMap<String, Data> input;
    private HashMap<String, Data> output;
    private HashMap<String,Property> properties;

    public Block(String name, String family, HashMap<String, Data> input, HashMap<String, Data> output, HashMap<String,Property> properties) {
        this.name = name;
        this.family = family;
        this.properties = properties;
        this.input = input;
        this.output = output;
    }

    public Block(String JSONObject){

    }

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

    public String toJS() {
        String js="blocks.register("+this.toJSON().toString()+");";
        return js;
    }

    public JSONObject toJSON(){
        JSONObject blockjs=new JSONObject();
        blockjs.put("name",getName());
        blockjs.put("family", getFamily());
        JSONArray fields=new JSONArray();
        for(String key:properties.keySet()){
            Property property=properties.get(key);
            JSONObject field=new JSONObject();
            field.put("name",property.getName());
            field.put("type",property.getType());
            field.put("defaultValue",property.getDefaultValue());
            field.put("attrs","editable");
            fields.put(field);
        }

        if(input!=null && input.size()!=0) {
            for(String input_param:input.keySet()) {
                Data input_value=input.get(input_param);
                JSONObject input_obj = new JSONObject();
                input_obj.put("name", input_value.getName());
                input_obj.put("type", input_value.getType());
                input_obj.put("attrs", "input");
                input_obj.put("card", input_value.getCardinality());
                fields.put(input_obj);
            }
        }

        if(output!=null && output.size()!=0) {
            for(String output_param:output.keySet()){
                Data output_value=output.get(output_param);
                JSONObject output_obj = new JSONObject();
                output_obj.put("name", output_value.getName());
                output_obj.put("type", output_value.getType());
                output_obj.put("attrs", "output");
                output_obj.put("card", output_value.getCardinality());
                fields.put(output_obj);
            }
        }
        blockjs.put("fields", fields);

        return blockjs;
    }

    public HashMap<String, Property> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, Property> properties) {
        this.properties = properties;
    }

    public HashMap<String, Data> getInput() {
        return input;
    }

    public void setInput(HashMap<String, Data> input) {
        this.input = input;
    }

    public HashMap<String, Data> getOutput() {
        return output;
    }

    public void setOutput(HashMap<String, Data> output) {
        this.output = output;
    }
}
