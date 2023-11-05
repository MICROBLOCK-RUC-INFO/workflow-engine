package com.wq.wfEngine.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wq.wfEngine.activiti.ActivitiUtils;
import com.wq.wfEngine.activiti.workflowFunction;
import com.wq.wfEngine.cache.cachedData;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.cache.oidEvents;
import org.activiti.engine.impl.interceptor.CommandContext;

public class userTaskComplete implements TaskListener {
    private Expression nextProcess;
    private Expression nextMessage;
    public void notify(DelegateTask task) {
        if (!task.getEventName().equals(TaskListener.EVENTNAME_COMPLETE)) {
            return;
        }
        CommandContext commandContext=Context.getCommandContext();
        RuntimeService runtimeService=ActivitiUtils.runtimeService;
        if (nextMessage!=null) {
            String messageName=nextMessage.getValue(task).toString();
            String oid=commandContext.getOid();
            Map<String,Object> variables=new HashMap<String,Object>(){{
                put("Oid",oid);
            }};//暂时没有放进去变量感觉并不需要
            runtimeService.messageEventReceived(messageName, oidEvents.getEventExecutionIdByOidAndName(oid, messageName), variables);
        }
        if (nextProcess!=null) {
            String processDefinitionKey=nextProcess.getValue(task).toString();
            String oid=commandContext.getOid();
            Map<String,Object> variables=new HashMap<String,Object>(){{
                put("Oid",oid);
            }};//暂时没有放进去变量感觉并不需要
            runtimeService.startProcessInstanceById(cachedData.getProcessId(processDefinitionKey,oid),variables);
        }
    }
}
