package org.activiti.engine.impl.cmd;



import java.util.List;

import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.redis.entityFieldMap;
import org.activiti.engine.impl.db.redis.useRedis;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InitRedisCmd implements Command<Void> {
  protected static final Logger LOGGER = LoggerFactory.getLogger(InitRedisCmd.class);

    public InitRedisCmd() {

    }
    @Override
    public Void execute(CommandContext commandContext) {

      DbSqlSession dbSqlSession= commandContext.getDbSqlSession();
      entityFieldMap.initEntityFieldMap();
      useRedis.initRedisFieldMap();
      List<String> ProcessDefinitionIds= dbSqlSession.selectAllProcessDefinitions();
      useRedis.initRedisNextId();
      dbSqlSession.selectAllProperties();
      dbSqlSession.selectAllResources();
      dbSqlSession.selectAllTasks();
      dbSqlSession.selectAllVariableInstances();
      dbSqlSession.selectAllDeployments();
      dbSqlSession.selectAllExecutions();
      dbSqlSession.selectAllEventSubscriptions();
      for (String processDefinitionId:ProcessDefinitionIds) {
        ProcessDefinitionUtil.getProcess(processDefinitionId);
      }
      LOGGER.info("init redis data success");
      return null;
    }
    
}
