package springcloud.springcloudgateway.workflow.threadExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

@Component
public class activitiChangeExecutor {
    private Logger logger=LoggerFactory.getLogger(activitiChangeExecutor.class);
    private Executor activitiChangeExecutor;
    public void executorInit() {
        ThreadFactory springThreadFactory = new CustomizableThreadFactory("activitiChange--");
        ThreadPoolExecutor taskExecutor = new ThreadPoolExecutor(2, 1000,
                60, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), springThreadFactory);
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        this.activitiChangeExecutor=taskExecutor;
        logger.info("activiti executor init successfully");
    }


    public Executor getExecutor() {
        return activitiChangeExecutor;
    }



    public void flush(Runnable runnable) {
        activitiChangeExecutor.execute(runnable);
    }
}