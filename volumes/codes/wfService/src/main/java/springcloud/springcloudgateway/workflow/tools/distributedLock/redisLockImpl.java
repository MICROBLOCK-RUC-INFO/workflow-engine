package springcloud.springcloudgateway.workflow.tools.distributedLock;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class redisLockImpl implements distributedLock{
    private final StringRedisTemplate redisLockClient=getRedisLockClient();
    private final String releaseLockLua="if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
    private final String lengthenLockLua="if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('expire', KEYS[1], ARGV[2]) else return 0 end";
    private ConcurrentHashMap<String,lockInfo> lockInfos=new ConcurrentHashMap<String,lockInfo>();
    static class lockInfo {
        String key;
        String value;
        long lastUpdatedTime;
        long lengthenInterval=40000L;

        public lockInfo(String key, String value,long lastUpdatedTime) {
            this.key=key;
            this.value=value;
            this.lastUpdatedTime=lastUpdatedTime;
        }

        public boolean isNeedUpate(long now) {
            return lastUpdatedTime+lengthenInterval<=now;
        }

        public void setUpdateTime(long time) {
            this.lastUpdatedTime=time;
        }
    }
    @Override
    public String tryLock(@NotNull String key) {
        String uuid=UUID.randomUUID().toString();
        boolean lockGet= redisLockClient.opsForValue().setIfAbsent(key, uuid, Duration.ofSeconds(60));
        if (lockGet) {
           lockInfos.put(key,new lockInfo(key, uuid, System.currentTimeMillis()));
           return uuid; 
        } else return null;
    }

    @Override
    public void releaseLock(String key,String lockValue) {
        if (lockValue==null) return;
        lockInfos.remove(key);
        DefaultRedisScript<Long> releaseScript=new DefaultRedisScript<Long>(releaseLockLua, Long.class);
        List<String> keys=new ArrayList<String>() {{
            add(key);
        }};
        long releaseRes=redisLockClient.execute(releaseScript, keys, lockValue);
    }


    public StringRedisTemplate getRedisLockClient() {
        RedisStandaloneConfiguration redisConfiguration=new RedisStandaloneConfiguration("10.77.70.121", 6379);
        redisConfiguration.setDatabase(0);
        
        LettuceConnectionFactory connectionFactory=new LettuceConnectionFactory(redisConfiguration);
        //LettuceConnectionFactory connectionFactory=new LettuceConnectionFactory(redisConfiguration);
        connectionFactory.afterPropertiesSet();
        StringRedisTemplate stringRedisTemplate=new StringRedisTemplate(connectionFactory);
        return stringRedisTemplate;
    }


    public boolean lengthenLock(String key, String value) {
        DefaultRedisScript<Long> lengthenScript=new DefaultRedisScript<Long>(lengthenLockLua, Long.class);
        List<String> keys=new ArrayList<String>() {{
            add(key);
        }};
        int lengthenRes=redisLockClient.execute(lengthenScript, keys, value, 60).intValue();
        return lengthenRes==1;
    }
    
    @Scheduled(fixedRate = 5000L)
    @Async("lengthenExecutor")
    public void loopLengthen() {
        Iterator<Entry<String, lockInfo>> entrys= lockInfos.entrySet().iterator();
        long now=System.currentTimeMillis();
        while (entrys.hasNext()) {
            lockInfo lockInfo=entrys.next().getValue();
            if (lockInfo.isNeedUpate(now)) {
                if (lengthenLock(lockInfo.key, lockInfo.value)) lockInfo.setUpdateTime(now);
            }
        }
    }

    @Bean("lengthenExecutor")
    public Executor lengthenExecutor() {
        ThreadPoolTaskExecutor executor=new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(1);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("lengthen-redisLock-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        return executor;
    }
}
