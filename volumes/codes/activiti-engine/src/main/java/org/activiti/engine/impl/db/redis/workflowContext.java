package org.activiti.engine.impl.db.redis;

import java.util.Timer;
import java.util.TimerTask;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.cache.ServiceCache;
import org.activiti.engine.impl.db.redis.timerTask.loopCleanCache;
import org.activiti.engine.impl.db.redis.tools.operation.handlers.serviceTaskBindHandler;
import org.activiti.engine.impl.db.redis.tools.operation.handlers.userTaskBindHandler;
import org.activiti.engine.impl.db.redis.tools.operation.operations.operationSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class workflowContext {
    private static operationSelector operationSelector;
    private static ServiceCache serviceCache;
    private static final Logger logger =LoggerFactory.getLogger(workflowContext.class);

    /**
     * @apiNote workflowContext这个类存在是为了没有那么多静态对象，
     * 使用workflowContext统一管理这些对象,本方法是workflowContext的初始化方法
     */
    public static void initWorkflowContext() {
        operationSelector=new operationSelector();
        personalConfig.initConfig();
        serviceCache=new ServiceCache();
        new loopCleanCache().start();
        logger.info("workflowContext init successfully");
    }

    public static ServiceCache getServiceCache() {
        return serviceCache;
    }

    public static operationSelector getOperationSelector() {
        return operationSelector;
    }

    public static userTaskBindHandler getUserTaskBindHandler() {
        return operationSelector.getUserTaskBindHandler();
    }

    public static serviceTaskBindHandler getServiceTaskBindHandler() {
        return operationSelector.getServiceTaskBindHandler();
    }

    
}
