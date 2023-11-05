package springcloud.springcloudgateway.workflow.tools.distributedLock;

import org.springframework.data.redis.core.StringRedisTemplate;

public interface distributedLock {
    /**
     * 
     * @param key 对key值进行加锁，举例如果是bind,则key为bind-oid-taskName
     * @return 如果加锁成功，返回加锁的key对应的value--随机生成的uuid,反之返回null
     */
    public String tryLock(String key);
    /**
     * 
     * @param key 要释放的锁的key
     * @param lockValue 要释放锁的value，只有locaValue与redis中的value对应时才能释放成功
     */
    public void releaseLock(String key,String lockValue);
}
