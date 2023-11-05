package springcloud.springcloudgateway.workflow.threadExecutor;


import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

import javax.annotation.Resource;
import org.springframework.stereotype.Service;

import springcloud.springcloudgateway.workflow.wfEngine;
import springcloud.springcloudgateway.workflow.helper.workflowFabric;
import springcloud.springcloudgateway.workflow.simulateCache.preExecutionCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;


public class runableManager {
    private Logger logger = LoggerFactory.getLogger(workflowFabric.class);

    private final String channelName="workflowchannel";

    public boolean startLoop(wfEngine wfEngine,long flushTimeInterval) {
        loopCheckForFlush loop=new loopCheckForFlush(wfEngine);
        Timer timer=new Timer();
        timer.schedule(loop, flushTimeInterval, flushTimeInterval);
        //现在都用
        timer.schedule(new LoopForBindFlush(wfEngine), flushTimeInterval/3, flushTimeInterval);
        logger.info("flush loop start successfully");
        return true;
    }

    class LoopForBindFlush extends TimerTask{
        private wfEngine wfEngine;
        public LoopForBindFlush(wfEngine wfEngine) {
            this.wfEngine=wfEngine;
        }
        @Override
        public void run() {
            wfEngine.getBindPackageData();
        } 
    }

    class loopCheckForFlush extends TimerTask {
        private wfEngine wf;
        public loopCheckForFlush(wfEngine wf) {
            this.wf=wf;
    
        }
        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                wf.preDatasToBlockChainAndFlush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
}
