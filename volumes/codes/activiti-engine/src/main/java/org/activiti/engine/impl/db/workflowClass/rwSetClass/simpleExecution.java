package org.activiti.engine.impl.db.workflowClass.rwSetClass;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(value = "execution")  
public class simpleExecution extends simpleEntity{
    private String activityId;
    private String oid;
    //processDefinitionId为deploymentName-processDefinitionKey
    private String pDid;
    //processInstanceId
    private String pIid;
    //rootProcessInstanceId
    private String rPIid;
    //
    

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }
}
