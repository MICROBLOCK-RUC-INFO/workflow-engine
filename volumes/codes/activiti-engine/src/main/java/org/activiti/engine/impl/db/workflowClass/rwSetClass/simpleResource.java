package org.activiti.engine.impl.db.workflowClass.rwSetClass;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(value = "resource")  
public class simpleResource extends simpleEntity{
    private String resourceName;

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    
}
