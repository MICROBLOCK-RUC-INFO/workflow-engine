package springcloud.springcloudgateway;

import org.hyperledger.fabric.sdk.workflow.workflowUse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import springcloud.springcloudgateway.workflow.simulateCache.*;
import springcloud.springcloudgateway.workflow.wfEngine;
import springcloud.springcloudgateway.workflow.context.wfEngineContext;
import springcloud.springcloudgateway.workflow.helper.wfConfig;
import springcloud.springcloudgateway.workflow.helper.workflowFabric;
import springcloud.springcloudgateway.workflow.threadExecutor.activitiChangeExecutor;
import springcloud.springcloudgateway.workflow.threadExecutor.flushThreadPool;
import springcloud.springcloudgateway.workflow.threadExecutor.runableManager;

@Component
public class ApplicationRunnerConfig implements ApplicationRunner {
    @Autowired
    workflowFabric workflowFabric;
    @Autowired
    wfEngine wfEngine;
    @Autowired
    wfConfig wfConfig;
    @Autowired
    flushThreadPool flushThreadPool;
    @Autowired
    activitiChangeExecutor activitiChangeExecutor;
    runableManager runableManager=new runableManager();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        workflowFabric.init();
        flushThreadPool.executorInit();
        activitiChangeExecutor.executorInit();
        runableManager.startLoop(wfEngine,wfConfig.getFlushTimeInterval());
        workflowUse.initSdkWorkflowUse();
        //wfEngineContext.init();
    }
}
