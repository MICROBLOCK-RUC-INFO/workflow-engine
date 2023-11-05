package org.activiti.engine.impl.db.workflowClass.rwSetClass;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME,property = "classType")
@JsonSubTypes({@JsonSubTypes.Type(value=simpleTask.class,name = "task"),
               @JsonSubTypes.Type(value=simpleExecution.class,name = "execution"),
               @JsonSubTypes.Type(value=simpleDeployment.class,name = "deployment"),
               @JsonSubTypes.Type(value=simpleResource.class,name = "resource"),
               @JsonSubTypes.Type(value=simpleProcessDefinition.class,name = "processDefinition"),
               @JsonSubTypes.Type(value=simpleEvent.class,name = "event"),
               @JsonSubTypes.Type(value=simpleVariable.class,name = "variable")}) 
public class simpleEntity {
    public final static String READ="read";
    public final static String WRITE_INSERT="w_insert";
    public final static String WRITE_UPDATE="w_update";
    public final static String WRITE_DELETE="w_delete";
    private String opType;

    public String getOpType() {
        return opType;
    }

    public void setOpType(String opType) {
        this.opType = opType;
    }

    
}
