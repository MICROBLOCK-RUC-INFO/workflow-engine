package org.activiti.engine.impl.cmd;

import org.activiti.engine.impl.db.cache.cachedFlushObjects;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;

public class flushCmd implements Command<Void>{

    private String[] oids;

    public flushCmd(String[] oids) {
        this.oids=oids;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        // TODO Auto-generated method stub
        cachedFlushObjects.flushCachedObjectsToRedis(commandContext,oids);
        return null;
    }
    
}
