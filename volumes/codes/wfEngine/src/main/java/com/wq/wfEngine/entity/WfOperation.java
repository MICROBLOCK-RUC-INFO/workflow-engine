package com.wq.wfEngine.entity;

import java.util.HashMap;

public class WfOperation {
    String type;
    String name;
    String key;
    HashMap<String,Object> variableMap = new HashMap<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public HashMap<String, Object> getVariableMap() {
        return variableMap;
    }

    public void setVariableMap(HashMap<String, Object> variableMap) {
        this.variableMap = variableMap;
    }
}
