package org.activiti.engine.impl.db.workflowClass.rwSetClass;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @apiNote 都是简单的实体，用于读写集
 */
@JsonTypeName(value = "deployment")  
public class simpleDeployment extends simpleEntity{
    private String deploymentName;

    public String getDeploymentName() {
        return deploymentName;
    }

    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }
    
}
