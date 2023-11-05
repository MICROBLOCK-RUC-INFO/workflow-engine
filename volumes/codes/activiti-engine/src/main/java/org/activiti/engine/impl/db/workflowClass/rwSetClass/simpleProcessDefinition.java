package org.activiti.engine.impl.db.workflowClass.rwSetClass;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(value = "processDefinition")  
public class simpleProcessDefinition extends simpleEntity{
    private String processDefinitionKey;
    private String resourceName;
    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }
    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }
    public String getResourceName() {
        return resourceName;
    }
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    
}
