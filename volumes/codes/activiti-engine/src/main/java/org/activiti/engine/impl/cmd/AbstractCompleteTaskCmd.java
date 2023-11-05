/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.cmd;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.IdentityLinkType;

import java.util.Map;

/**

 */
public abstract class AbstractCompleteTaskCmd extends NeedsActiveTaskCmd<Void> {

  private static final long serialVersionUID = 1L;

  public AbstractCompleteTaskCmd(String taskId) {
    super(taskId);
  }

  protected void executeTaskComplete(CommandContext commandContext, TaskEntity taskEntity, Map<String, Object> variables, boolean localScope) {
    // Task complete logic

    if (taskEntity.getDelegationState() != null && taskEntity.getDelegationState().equals(DelegationState.PENDING)) {
      throw new ActivitiException("A delegated task cannot be completed, but should be resolved instead.");
    }

    commandContext.getProcessEngineConfiguration().getListenerNotificationHelper().executeTaskListeners(taskEntity, TaskListener.EVENTNAME_COMPLETE);//sunzhouxing:执行tasklistener
    if (Authentication.getAuthenticatedUserId() != null && taskEntity.getProcessInstanceId() != null) {
      ExecutionEntity processInstanceEntity = commandContext.getExecutionEntityManager().findById(taskEntity.getProcessInstanceId());//sunzhouxing:查询processinstance
      commandContext.getIdentityLinkEntityManager().involveUser(processInstanceEntity, Authentication.getAuthenticatedUserId(),IdentityLinkType.PARTICIPANT);
    }//sunzhouxing:identityLink表没有用到。暂时把这一段应该条件应该是判断不通过跳过，没有看到setAuthenticatedUserId

    //sunzhouxing:这个监听器暂时没用到注释掉。
    ActivitiEventDispatcher eventDispatcher = Context.getProcessEngineConfiguration().getEventDispatcher();
    if (eventDispatcher.isEnabled()) {
      if (variables != null) {
        eventDispatcher.dispatchEvent(ActivitiEventBuilder.createEntityWithVariablesEvent(ActivitiEventType.TASK_COMPLETED, taskEntity, variables, localScope));
      } else {
        eventDispatcher.dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.TASK_COMPLETED, taskEntity));
      }
    }

    commandContext.getTaskEntityManager().deleteTask(taskEntity, null, false, false);//sunzhouxing:这里是删除
    

    // Continue process (if not a standalone task)
    if (taskEntity.getExecutionId() != null) {
      //sunzhouxing:自己加的
      // ExecutionEntity executionEntity = (ExecutionEntity)commandContext.getDbSqlSession().findByIdInRedis(ExecutionEntityImpl.class.toString(), taskEntity.getExecutionId());
      // if (executionEntity==null) {
      //   executionEntity = commandContext.getExecutionEntityManager().findById(taskEntity.getExecutionId());
      // }
      ExecutionEntity executionEntity = commandContext.getExecutionEntityManager().findById(taskEntity.getExecutionId());//原版sunzhouxing:这是得到的与task相关联的execution
      Context.getAgenda().planTriggerExecutionOperation(executionEntity);
      /*
      *sunzhouxing:这一句应该是complete后续的内容,会创建一个由commandtext(default)与excution与agenda组成的abstractoperation类
      *加入到list<runnable>线程中等待运行
      *先回去执行一个寻找outgoingflow的operation,然后根据outgoingflow去执行一个continueprocess的operation
      */
    }
    return;
  }

}
