package org.activiti.engine.impl.db.workflowClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.impl.db.redis.useRedis;
import org.activiti.engine.impl.persistence.entity.DeploymentEntityImpl;
import org.activiti.engine.impl.persistence.entity.Entity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.activiti.engine.impl.persistence.entity.PropertyEntityImpl;
import org.activiti.engine.impl.persistence.entity.ResourceEntityImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntityImpl;

public class typeTransfer {
    public final static String deployment="d";
    public final static String execution="e";
    public final static String processDefinition="p";
    public final static String resource="r";
    public final static String task="t";
    public final static String variable="v";
    public final static String property="pp";
    public final static String event="eve";

    private final static String signalEvent="class org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntityImpl";
    private final static String compensateEvent="class org.activiti.engine.impl.persistence.entity.CompensateEventSubscriptionEntityImpl";
    private final static String messageEvent="class org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntityImpl";
    private final List<String> insertOrder=new ArrayList<String>() {
        {
            add("pp");
            add("d");
            add("p");
            add("r");
            add("e");
            add("t");
            add("v");
            add("eve");
        }
    };
    private final List<String> deleteOrder=new ArrayList<String>() {
        {
            add("eve");
            add("v");
            add("t");
            add("e");
            add("r");
            add("p");
            add("d");
            add("pp");
        };
    };
    public static final Map<String,String> getSimpleType=new HashMap<String,String>(){
        {
            put(useRedis.DeploymentClass,deployment);
            put(useRedis.ExecutionClass,execution);
            put(useRedis.ProcessDefinitionClass,processDefinition);
            put(useRedis.ResourceClass,resource);
            put(useRedis.TaskClass,task);
            put(useRedis.VariableClass,variable);
            put(useRedis.PropertyClass,property);
            put(useRedis.EventClass,event);
            put(messageEvent,event);
            put(signalEvent,event);
            put(compensateEvent,event);
        };
    };
    public static final Map<String,String> getEntityType=new HashMap<String,String>(){
        {
            put(deployment,useRedis.DeploymentClass);
            put(execution,useRedis.ExecutionClass);
            put(processDefinition,useRedis.ProcessDefinitionClass);
            put(resource,useRedis.ResourceClass);
            put(task,useRedis.TaskClass);
            put(variable,useRedis.VariableClass);
            put(property,useRedis.PropertyClass);
            put(event,useRedis.EventClass);
        };
    };

}
