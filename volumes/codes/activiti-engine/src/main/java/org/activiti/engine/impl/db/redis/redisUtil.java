package org.activiti.engine.impl.db.redis;

import java.net.ConnectException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisSocketConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;


public class redisUtil {
    @SuppressWarnings("rawtypes")
    public StringRedisTemplate getstringRedisTemplate() {
        RedisStandaloneConfiguration redisConfiguration=new RedisStandaloneConfiguration("localhost", 6379);
        redisConfiguration.setDatabase(0);
        
        LettuceConnectionFactory connectionFactory=new LettuceConnectionFactory(new RedisSocketConfiguration("/home/sunweekstar/redis/redis.sock"));
        //LettuceConnectionFactory connectionFactory=new LettuceConnectionFactory(redisConfiguration);
        connectionFactory.afterPropertiesSet();
        StringRedisTemplate stringRedisTemplate=new StringRedisTemplate(connectionFactory);
        return stringRedisTemplate;
    } 

    @SuppressWarnings("rawtypes")
    public StringRedisTemplate getTableRedisClient() {
        RedisStandaloneConfiguration redisConfiguration=new RedisStandaloneConfiguration("localhost", 6379);
        redisConfiguration.setDatabase(1);
        
        LettuceConnectionFactory connectionFactory=new LettuceConnectionFactory(new RedisSocketConfiguration("/home/sunweekstar/redis/redis.sock"));
        //LettuceConnectionFactory connectionFactory=new LettuceConnectionFactory(redisConfiguration);
        connectionFactory.afterPropertiesSet();
        StringRedisTemplate stringRedisTemplate=new StringRedisTemplate(connectionFactory);
        return stringRedisTemplate;
    } 

    // public static Jedis get() {
    //     JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
    //     // 最大空闲数
    //     jedisPoolConfig.setMaxIdle(1);
    //     // 连接池的最大数据库连接数
    //     jedisPoolConfig.setMaxTotal(maxTotal);
    //     // 最大建立连接等待时间
    //     jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
    //     // 逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
    //     jedisPoolConfig.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
    //     // 每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
    //     jedisPoolConfig.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
    //     // 逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
    //     jedisPoolConfig.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
    //     // 是否在从池中取出连接前进行检验,如果检验失败,则从池中去除连接并尝试取出另一个
    //     jedisPoolConfig.setTestOnBorrow(testOnBorrow);
    //     // 在空闲时检查有效性, 默认false
    //     jedisPoolConfig.setTestWhileIdle(testWhileIdle);
    //     return jedisPoolConfig;
    // }

    // public static Jedis getMutilSetJedis() {
    //     return new Jedis("localhost", 6379);
    // }

    // public static Jedis getGetJedis() {
    //     return new Jedis("localhost", 6379);
    // }

    // public static Jedis getDeleteJedis() {
    //     return new Jedis("localhost", 6379);
    // }

    // public static Jedis getEntityIdJedis() {
    //     return new Jedis("localhost", 6379);
    // }

    // public static Jedis getSingleSetJedis() {
    //     return new Jedis("localhost", 6379);
    // }

    // public static Jedis getOtherJedis() {
    //     return new Jedis("localhost", 6379);
    // }

    // public static Map<String,Jedis> getGetJedisMap() {
    //     Map<String,Jedis> getJedisMap=new HashMap<>();
    //     getJedisMap.put(useRedis.TaskClass,new Jedis("localhost",6379));
    //     getJedisMap.put(useRedis.VariableClass,new Jedis("localhost",6379));
    //     getJedisMap.put(useRedis.ExecutionClass,new Jedis("localhost",6379));
    //     getJedisMap.put(useRedis.ProcessDefinitionClass,new Jedis("localhost",6379));
    //     getJedisMap.put(useRedis.DeploymentClass,new Jedis("localhost",6379));
    //     getJedisMap.put(useRedis.ResourceClass,new Jedis("localhost",6379));
    //     getJedisMap.put(useRedis.PropertyClass,new Jedis("localhost",6379));
    //     getJedisMap.put(useRedis.EventClass,new Jedis("localhost",6379));
    //     return getJedisMap;
    // }
}
