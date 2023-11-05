package org.activiti.engine.impl.db.redis.tools.rwSet.extractor;

import java.util.Iterator;
import java.util.List;

import org.activiti.engine.impl.db.workflowClass.rwSetClass.*;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.DeploymentEntityImpl;
import org.activiti.engine.impl.persistence.entity.Entity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntityImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntityImpl;

public class extractor {

    protected static void handleEntities(Entity typeEntity,Iterator<Entity> entities,List<simpleEntity> extractList,String writeType) {
        if (typeEntity instanceof TaskEntityImpl) {
            while (entities.hasNext()) {
                simpleEntity simpleEntity=handleTask((TaskEntity)entities.next());
                simpleEntity.setOpType(writeType);
                extractList.add(simpleEntity);
            }
        } else if (typeEntity instanceof ExecutionEntityImpl) {
            while (entities.hasNext()) {
                simpleEntity simpleEntity=handleExecution((ExecutionEntity)entities.next());
                simpleEntity.setOpType(writeType);
                extractList.add(simpleEntity);
            }
        } else if (typeEntity instanceof EventSubscriptionEntityImpl) {
            while (entities.hasNext()) {
                simpleEntity simpleEntity=handleEvent((EventSubscriptionEntity)entities.next());
                simpleEntity.setOpType(writeType);
                extractList.add(simpleEntity);
            }
        } else if (typeEntity instanceof DeploymentEntityImpl) {
            while (entities.hasNext()) {
                simpleEntity simpleEntity=handleDeployment((DeploymentEntity)entities.next());
                simpleEntity.setOpType(writeType);
                extractList.add(simpleEntity);
            }
        } else if (typeEntity instanceof ProcessDefinitionEntityImpl) {
            while (entities.hasNext()) {
                simpleEntity simpleEntity=handleDefinition((ProcessDefinitionEntity)entities.next());
                simpleEntity.setOpType(writeType);
                extractList.add(simpleEntity);
            }
        } else if (typeEntity instanceof ResourceEntityImpl) {
            while (entities.hasNext()) {
                simpleEntity simpleEntity=handleResource((ResourceEntity)entities.next());
                simpleEntity.setOpType(writeType);
                extractList.add(simpleEntity);
            }
        } else if (typeEntity instanceof VariableInstanceEntityImpl) {
            while (entities.hasNext()) {
                simpleEntity simpleEntity=handleVariable((VariableInstanceEntity)entities.next());
                simpleEntity.setOpType(writeType);
                extractList.add(simpleEntity);
            }
        }
    }

    protected static simpleEntity handleEntity(Entity entity) {
        if (entity instanceof TaskEntityImpl) {
            return handleTask((TaskEntity)entity);
        } else if (entity instanceof ExecutionEntityImpl) {
            return handleExecution((ExecutionEntity)entity);
        } else if (entity instanceof EventSubscriptionEntityImpl) {
            return handleEvent((EventSubscriptionEntity)entity);
        } else if (entity instanceof DeploymentEntityImpl) {
            return handleDeployment((DeploymentEntity)entity);
        } else if (entity instanceof ProcessDefinitionEntityImpl) {
            return handleDefinition((ProcessDefinitionEntity)entity);
        } else if (entity instanceof ResourceEntityImpl) {
            return handleResource((ResourceEntity)entity);
        } else if (entity instanceof VariableInstanceEntityImpl) {
            return handleVariable((VariableInstanceEntity)entity);
        } else {
            return null;
        }
    }

    private static simpleTask handleTask(TaskEntity taskEntity) {
        simpleTask task=new simpleTask();
        task.setOid(taskEntity.getOid());
        task.setTaskName(taskEntity.getName());
        task.setUserId(taskEntity.getUserId());
        task.setDefinitionKey(taskEntity.getTaskDefinitionKey());
        return task;
    }

    private static simpleExecution handleExecution(ExecutionEntity executionEntity) {
        simpleExecution execution=new simpleExecution();
        execution.setOid(executionEntity.getOid());
        execution.setActivityId(executionEntity.getCurrentActivityId());
        return execution;
    }

    private static simpleEvent handleEvent(EventSubscriptionEntity eventEntity) {
        simpleEvent event=new simpleEvent();
        event.setOid(eventEntity.getOid());
        event.setEventType(eventEntity.getEventType());
        event.setEventName(eventEntity.getEventName());
        event.setActivityId(eventEntity.getActivityId());
        return event;
    }

    private static simpleDeployment handleDeployment(DeploymentEntity deploymentEntity) {
        simpleDeployment deployment=new simpleDeployment();
        deployment.setDeploymentName(deploymentEntity.getName());
        return deployment;
    }

    private static simpleProcessDefinition handleDefinition(ProcessDefinitionEntity processDefinitionEntity) {
        simpleProcessDefinition processDefinition=new simpleProcessDefinition();
        processDefinition.setProcessDefinitionKey(processDefinitionEntity.getKey());
        processDefinition.setResourceName(processDefinitionEntity.getResourceName());
        return processDefinition;
    }

    private static simpleResource handleResource(ResourceEntity resourceEntity) {
        simpleResource resource=new simpleResource();
        resource.setResourceName(resourceEntity.getName());
        return resource;
    }

    private static simpleVariable handleVariable(VariableInstanceEntity variableEntity) {
        simpleVariable variable=new simpleVariable();
        variable.setName(variableEntity.getName());
        variable.setVariableType(variableEntity.getTypeName());
        variable.setValue(variableEntity.getValue());
        return variable;
    }
}
