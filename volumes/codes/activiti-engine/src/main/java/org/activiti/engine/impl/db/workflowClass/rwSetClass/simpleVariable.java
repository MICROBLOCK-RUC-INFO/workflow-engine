package org.activiti.engine.impl.db.workflowClass.rwSetClass;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(value = "variable")  
public class simpleVariable extends simpleEntity{
    private String variableType;
    private String name;
    private Object value;
    public String getVariableType() {
        return variableType;
    }
    public void setVariableType(String variableType) {
        this.variableType = variableType;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }

    
}
