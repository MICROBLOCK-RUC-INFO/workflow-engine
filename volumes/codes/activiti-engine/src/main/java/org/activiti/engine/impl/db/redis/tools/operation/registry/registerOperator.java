package org.activiti.engine.impl.db.redis.tools.operation.registry;

import org.activiti.engine.impl.db.redis.redisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @apiNote 组织注册
 */
public class registerOperator {
    private final static StringRedisTemplate redisClient=redisUtil.getTableRedisClient();
    private final static Logger logger=LoggerFactory.getLogger(registerOperator.class);

    private final static String PRIVATEKEY="pri";
    private final static String PUBLICKEY="pub";

    /**
     * @apiNote 看是否注册
     * @param name
     * @return 如果未注册返回null,已注册返回老私钥，检查私钥是否一致
     */
    public static String isRegistered(String name) {
        Object oldPriKey= redisClient.opsForHash().get(name, PRIVATEKEY);
        return oldPriKey==null?null:String.valueOf(oldPriKey);
    }

    /**
     * @apiNote 将组织名，公私钥写入redis
     */
    public static void register(String name,String publicKey,String privateKey) {
        redisClient.opsForHash().put(name, PRIVATEKEY, privateKey);
        redisClient.opsForHash().put(name,PUBLICKEY,publicKey);
    }
}
