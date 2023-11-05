package org.activiti.engine.impl.cmd;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteFromRedisCmd implements Command<Void> {
    protected static final Logger LOGGER=LoggerFactory.getLogger(DeleteFromRedisCmd.class);

    public DeleteFromRedisCmd() {

    }
    @Override
    public Void execute(CommandContext commandContext) {
        commandContext.getDbSqlSession().flushRedis();
        return null;
    }
}
