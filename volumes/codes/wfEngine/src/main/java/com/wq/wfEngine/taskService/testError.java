package com.wq.wfEngine.taskService;

import java.util.Map;

import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;

import com.wq.wfEngine.tool.jsonTransfer;
import com.wq.wfEngine.tool.serviceComposition.inputSelectParser;

public class testError implements JavaDelegate{
    private Expression input;
    
    private Expression output;
    public void execute(DelegateExecution execution) {
        CommandContext commandContext=Context.getCommandContext();
        Map<String,Object> dataPool=commandContext.getCachedOutput();
        String body=inputSelectParser.parse(dataPool, input.getValue(execution).toString());
        Map<String,Object> bodyMap=jsonTransfer.jsonToMap(body);
        String userId=String.valueOf(bodyMap.get("userId"));
        String cardId=String.valueOf(bodyMap.get("cardId"));
        if (!userId.equals(cardId)) {
            throw new BpmnError("testError", "testOK");
        } else {
            if (output!=null) {
                //如果有指定输出
                String outputString=inputSelectParser.parse(dataPool, output.getValue(execution).toString());
                commandContext.setLastResponse(outputString);
            }
        }
    }
}
