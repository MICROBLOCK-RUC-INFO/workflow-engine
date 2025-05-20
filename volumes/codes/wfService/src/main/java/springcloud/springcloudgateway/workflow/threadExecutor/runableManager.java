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

/**
 * 2025/4/10
 * 用于管理隔一段时间打大包并开始上链和flush的定时任务。
 * 整个过程就是，定时任务会定时收集要上链的数据，打个大包，然后创建一个上链线程，将上链线程交给上链线程池来执行。
 * 上链线程结束时会创建一个flush线程，将执行结果传递给flush线程，将flush线程交给flush线程池来执行
 */
public class runableManager {
    private Logger logger = LoggerFactory.getLogger(workflowFabric.class);

    private final String channelName="workflowchannel";

    public boolean startLoop(wfEngine wfEngine,long flushTimeInterval) {
        loopCheckForFlush loop=new loopCheckForFlush(wfEngine);
        Timer timer=new Timer();
         //创建定时任务
        timer.schedule(loop, flushTimeInterval, flushTimeInterval);
        timer.schedule(new LoopForBindFlush(wfEngine), flushTimeInterval/3, flushTimeInterval);
        logger.info("flush loop start successfully");
        return true;
    }

/**
 * 分了两个写的主要原因之前也提过deploy,instance,complete的模拟执行结果太大是缓存在在本地的，
 * 第二个也是一开始没有想到兼容的问题，后面又有服务注册，绑定的需求就分开写了
 */

    /**
     * 内部类，定时任务，对应除deploy,instance,complete的其他
     */
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

    /**
     * 内部类，定时任务，对应deploy,instance,complete
     */
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
