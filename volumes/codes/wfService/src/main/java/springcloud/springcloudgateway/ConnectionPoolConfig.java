package springcloud.springcloudgateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.*;

/**
 * @Author: 李浩然
 * @Date: 2021/4/22 8:57 下午
 */
@Configuration
@EnableAsync
public class ConnectionPoolConfig {
    @Bean("connectionExecutor")
    public Executor connectionExecutror() {
        ThreadFactory springThreadFactory = new CustomizableThreadFactory("monitorService--");
        ThreadPoolExecutor taskExecutor = new ThreadPoolExecutor(0, 1000,
                60, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), springThreadFactory);
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return taskExecutor;
    }
}