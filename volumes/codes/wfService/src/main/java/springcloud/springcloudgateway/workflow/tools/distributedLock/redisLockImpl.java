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
/*
 * 2025/4/1
 * 使用Lua脚本的redis分布式锁实现
 * 已弃用
 */
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
    /*
     * 加锁
     */
    @Override
    public String tryLock(@NotNull String key) {
        //生成唯一ID
        String uuid=UUID.randomUUID().toString();
        /*
        *查看Redis中Key是否存在，若Key值存在，则说明锁已存在，返回false,
        *若Key值不存在，则将Key与UUID写入Redis,返回true
        */
        boolean lockGet= redisLockClient.opsForValue().setIfAbsent(key, uuid, Duration.ofSeconds(60));
        if (lockGet) {
            //将锁放入缓存，并返回UUID，用于释放锁的时候确认身份
           lockInfos.put(key,new lockInfo(key, uuid, System.currentTimeMillis()));
           return uuid; 
        } else return null;
    }

    /*
     * 锁的释放，不用担心释放失败，因为设置了过期时间。
     * 对Redis的操作目前是即时的。
     * 如果后续需要使用且对性能有要求，可以考虑使用一个线程来定时操作Redis释放锁
     */
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


    /*
     * 获得Redis Client端
     */
    public StringRedisTemplate getRedisLockClient() {
        RedisStandaloneConfiguration redisConfiguration=new RedisStandaloneConfiguration("10.77.70.121", 6379);
        redisConfiguration.setDatabase(0);
        
        LettuceConnectionFactory connectionFactory=new LettuceConnectionFactory(redisConfiguration);
        //LettuceConnectionFactory connectionFactory=new LettuceConnectionFactory(redisConfiguration);
        connectionFactory.afterPropertiesSet();
        StringRedisTemplate stringRedisTemplate=new StringRedisTemplate(connectionFactory);
        return stringRedisTemplate;
    }


    /*
     * 延长锁过期时间
     */
    public boolean lengthenLock(String key, String value) {
        DefaultRedisScript<Long> lengthenScript=new DefaultRedisScript<Long>(lengthenLockLua, Long.class);
        List<String> keys=new ArrayList<String>() {{
            add(key);
        }};
        int lengthenRes=redisLockClient.execute(lengthenScript, keys, value, 60).intValue();
        return lengthenRes==1;
    }
    
    /*
     * 对所有缓存中的锁，延长过期时间
     * 避免还未释放锁，锁就过期了，造成冲突
     */
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

    /*
     * 延长锁操作线程池设置
     */
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
