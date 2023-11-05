package org.activiti.engine.impl.cmd;

import org.activiti.engine.impl.db.cache.cachedFlushObjects;
import org.activiti.engine.impl.db.workflowClass.cachedResponse;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;

public class getWorkflowResponseCmd implements Command<cachedResponse>{
    private String key;
    public getWorkflowResponseCmd(String key) {
        this.key=key;
    }

    @Override
    public cachedResponse execute(CommandContext commandContext) {
        cachedResponse response= cachedFlushObjects.getWorkflowResponse(key);     
        return response;
    }
}
