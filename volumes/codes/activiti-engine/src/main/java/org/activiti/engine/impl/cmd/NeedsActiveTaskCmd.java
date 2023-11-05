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

import java.io.Serializable;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.db.redis.useRedis;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.activiti.engine.task.Task;

/**
 * An abstract superclass for {@link Command} implementations that want to verify the provided task is always active (ie. not suspended).
 * 

 */
public abstract class NeedsActiveTaskCmd<T> implements Command<T>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String taskId;

  public NeedsActiveTaskCmd(String taskId) {
    this.taskId = taskId;
  }

  public T execute(CommandContext commandContext) {
    if (taskId == null) {
      throw new ActivitiIllegalArgumentException("taskId is null");
    }

    //sunzhouxing:自己加的先查redis数据库，没有再通过mybatis去查询
    // TaskEntity task=(TaskEntity)commandContext.getDbSqlSession().findByIdInRedis(TaskEntityImpl.class.toString(), taskId);
    // if (task==null) {
    //   task = commandContext.getTaskEntityManager().findById(taskId);
    // }
    
    TaskEntity task = commandContext.getTaskEntityManager().findById(taskId);//原版//sunzhouxing:taskcomplete过程中先通过这个寻找到taskEntity 5-8 第一步数据库操作

  
    if (task == null) {
      throw new ActivitiObjectNotFoundException("Cannot find task with id " + taskId, Task.class);
    }

    if (task.isSuspended()) {
      throw new ActivitiException(getSuspendedTaskException());
    }

    return execute(commandContext, task);//sunzhouxing:这里转到CompleteTaskCmd的execute 
  }

  /**
   * Subclasses must implement in this method their normal command logic. The provided task is ensured to be active.
   */
  protected abstract T execute(CommandContext commandContext, TaskEntity task);

  /**
   * Subclasses can override this method to provide a customized exception message that will be thrown when the task is suspended.
   */
  protected String getSuspendedTaskException() {
    return "Cannot execute operation: task is suspended";
  }

}
