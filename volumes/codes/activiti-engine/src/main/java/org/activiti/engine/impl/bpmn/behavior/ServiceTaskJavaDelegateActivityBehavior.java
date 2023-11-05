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

package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.delegate.invocation.JavaDelegateInvocation;
import org.activiti.engine.impl.interceptor.CommandContext;

/**

 */
public class ServiceTaskJavaDelegateActivityBehavior extends TaskActivityBehavior implements ActivityBehavior, ExecutionListener {

  private static final long serialVersionUID = 1L;
  
  protected JavaDelegate javaDelegate;

  protected ServiceTaskJavaDelegateActivityBehavior() {
  }

  public ServiceTaskJavaDelegateActivityBehavior(JavaDelegate javaDelegate) {
    this.javaDelegate = javaDelegate;
  }

  public void execute(DelegateExecution execution) {
    /**
     * 孙周星
     * 2022年9月19日 11：56
     * 这一段是serviceTask的行为
     * 调用的是接口javaDelegate的excute方法
     * 外部可以实现这个接口，我在外部实现接口并重载
     */
    //System.out.println("enter.................");
    CommandContext commandContext=Context.getCommandContext();
    Context.getProcessEngineConfiguration().getDelegateInterceptor().handleInvocation(new JavaDelegateInvocation(javaDelegate, execution));
    leave(execution);
  }

  public void notify(DelegateExecution execution) {
    execute(execution);
  }
}
