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

import java.util.Map;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.bpmn.helper.TaskVariableCopier;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.redis.tools.jackJson.jsonTransfer;


/**

 */
public class CompleteTaskCmd extends AbstractCompleteTaskCmd {

  private static final long serialVersionUID = 1L;
  protected Map<String, Object> variables;
  protected Map<String, Object> transientVariables;
  protected boolean localScope;

  public CompleteTaskCmd(String taskId, Map<String, Object> variables) {
    super(taskId);
    this.variables = variables;
  }

  public CompleteTaskCmd(String taskId, Map<String, Object> variables, boolean localScope) {
    this(taskId, variables);
    this.localScope = localScope;
  }
  
  public CompleteTaskCmd(String taskId, Map<String, Object> variables, Map<String, Object> transientVariables) {
    this(taskId, variables);
    this.transientVariables = transientVariables;
  }

  protected Void execute(CommandContext commandContext, TaskEntity task) {
    if (variables!=null) {
      if (variables.containsKey("Oid")) {
        commandContext.setOid(String.valueOf(variables.remove("Oid")));
      }
      if (variables.containsKey("businessData")) {
        commandContext.setBusinessData(String.valueOf(variables.remove("businessData")));
      }
      if (variables.containsKey("serviceTaskResultJson")) {
        commandContext.setServiceTaskResults(jsonTransfer.jsonToServiceTaskRes(String.valueOf(variables.remove("serviceTaskResultJson"))));
      }
      if (variables.containsKey("user")) {
        String user=String.valueOf(variables.remove("user"));
        if (!user.equals(task.getUserId())) {
          if (!task.getUserId().equals("anyone")) {
            throw new RuntimeException("userTask:"+task.getName()+" belong to the user:"+task.getUserId()+",but the requester is "+user);
          }
        }
      }
    }
    commandContext.setSegmentedExecution(true);//使用分段flush
    if (variables != null) {
    	if (localScope) {
    		task.setVariablesLocal(variables);
    	} else if (task.getExecutionId() != null) {
    		task.setExecutionVariables(variables);
    	} else {
    		task.setVariables(variables);
    	}
    }//
    
    if (transientVariables != null) {
      if (localScope) {
        task.setTransientVariablesLocal(transientVariables);
      } else {
        task.setTransientVariables(transientVariables);
      }
    }

    if(commandContext.getProcessEngineConfiguration().isCopyVariablesToLocalForTasks()){
      TaskVariableCopier.copyVariablesOutFromTaskLocal(task);
    }

    executeTaskComplete(commandContext, task, variables, localScope);//sunzhouxing:这里转到AbstractCompleteTaskCmd的 executeTaskComplete
    return null;
  }

  @Override
  protected String getSuspendedTaskException() {
    return "Cannot complete a suspended task";
  }

}
