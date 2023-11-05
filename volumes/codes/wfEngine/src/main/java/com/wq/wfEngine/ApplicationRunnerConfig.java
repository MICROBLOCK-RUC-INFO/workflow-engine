package com.wq.wfEngine;

import org.activiti.engine.impl.db.redis.workflowContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.wq.wfEngine.cache.monitorIps;
import com.wq.wfEngine.config.NodeNumConfig;
import com.wq.wfEngine.config.evilNodeConfig;
import com.wq.wfEngine.extend.justLoop;


@Component
public class ApplicationRunnerConfig implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        //NodeNumConfig.initNodeInfo();
        monitorIps.initMonitorIps();
        evilNodeConfig.init();
        workflowContext.initWorkflowContext();
        new justLoop().start();
    }
}
