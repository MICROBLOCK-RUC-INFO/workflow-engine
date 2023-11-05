package org.activiti.engine.impl.db.workflowClass.rwSetClass;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(value = "task")  
public class simpleTask extends simpleEntity{
    private String taskName;
    private String userId;
    private String definitionKey;
    private String oid;

    

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDefinitionKey() {
        return definitionKey;
    }

    public void setDefinitionKey(String definitionKey) {
        this.definitionKey = definitionKey;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }
}
