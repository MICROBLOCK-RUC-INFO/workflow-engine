package springcloud.springcloudgateway.workflow.threadExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

@Component
public class flushThreadPool {
    private Logger logger=LoggerFactory.getLogger(flushThreadPool.class);
    private Executor flushExecutor;
    public void executorInit() {
        ThreadFactory springThreadFactory = new CustomizableThreadFactory("flush--");
        ThreadPoolExecutor taskExecutor = new ThreadPoolExecutor(3, 1000,
                60, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), springThreadFactory);
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        this.flushExecutor=taskExecutor;
        logger.info("flush executor init successfully");
    }

    public void flush(Runnable runnable) {
        flushExecutor.execute(runnable);
    }
}
