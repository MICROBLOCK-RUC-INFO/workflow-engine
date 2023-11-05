package com.wq.wfEngine.taskService;


import java.util.List;
import java.util.Map;

import com.wq.wfEngine.activiti.ActivitiUtils;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;

public class test implements JavaDelegate {
    private Expression nextMessage;
    private Expression nextProcess;
    private Expression nextSignal;
    private Expression test;

    public void execute(DelegateExecution execution) {
        TaskService taskService=ActivitiUtils.taskService;
        System.out.println("开始"+test.getValue(execution).toString());
        
        RuntimeService runtimeService=ActivitiUtils.runtimeService;
        Map<String,Object> variables=runtimeService.getVariables(execution.getId());
        runtimeService.completeAdhocSubProcess(execution.getId());
        if (nextMessage!=null) {
            System.out.println(nextMessage.getValue(execution).toString());
            System.out.println(execution.getEventName() + "开始了");
            System.out.println(runtimeService.getVariable(execution.getId(), "insName", String.class));
            List<ProcessInstance> processInstanceList = runtimeService.createProcessInstanceQuery()
                // .processInstanceNameLike(variables.get("insName").toString() + "%")
                .list();
            System.out.println(processInstanceList.toString()+"%%%%");
            String messageStr = nextMessage.getValue(execution).toString();
            System.out.println(messageStr);
            for (ProcessInstance subProcessInstance : processInstanceList) {
                List<Execution> executions = runtimeService.createExecutionQuery()
                    .messageEventSubscriptionName(messageStr)
                    // .processInstanceId(subProcessInstance.getId())
                    .list();
                System.out.println(executions.toString()+"!!!");
                for (Execution exe : executions) {
                    if (exe != null) {
                        if (runtimeService.getVariable(exe.getId(), "insName", String.class).equals(variables.get("insName").toString())) {
                            System.out.println(variables.get("insName").toString());
                            runtimeService.messageEventReceived(messageStr, exe.getId(),
                            variables);
                        }
                    }
                }
            }
            System.out.println(execution.getEventName() + "结束了");
            System.out.println("最后最后");
        }
        if (nextProcess!=null) {
            System.out.println(nextProcess.getValue(execution).toString());
            ProcessInstance subProcessInstance = runtimeService
                .startProcessInstanceByKey(nextProcess.getValue(execution).toString(), variables);
            if (!subProcessInstance.isEnded()) {
                runtimeService.setProcessInstanceName(subProcessInstance.getId(),
                variables.get("insName").toString() + ':' + subProcessInstance.getProcessDefinitionKey());
            }
        }
        System.out.println("结束"+test.getValue(execution).toString());
    }
}
