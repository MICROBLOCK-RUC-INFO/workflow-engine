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

/**
 * 2025/4/10
 * flush线程池
 */
@Component
public class activitiChangeExecutor {
    private Logger logger=LoggerFactory.getLogger(activitiChangeExecutor.class);
    private Executor activitiChangeExecutor;
    /**
     * 创建
     */
    public void executorInit() {
        //前缀名设置
        ThreadFactory springThreadFactory = new CustomizableThreadFactory("activitiChange--");
        //线程池创建
        ThreadPoolExecutor taskExecutor = new ThreadPoolExecutor(2, 1000,
                60, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), springThreadFactory);
        //设置拒绝策略(线程不够用且达到最大线程数)，这里设置的新建一个线程直接运行
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