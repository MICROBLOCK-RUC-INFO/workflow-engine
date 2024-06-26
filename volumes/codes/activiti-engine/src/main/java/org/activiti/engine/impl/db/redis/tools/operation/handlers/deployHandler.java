package org.activiti.engine.impl.db.redis.tools.operation.handlers;

import java.util.Map;

import org.activiti.engine.impl.db.redis.redisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

public class deployHandler implements handler{
    private final static StringRedisTemplate redisClient=redisUtil.getTableRedisClient();
    private final static Logger logger=LoggerFactory.getLogger(deployHandler.class);
    

    @Override
    public String simulate(Map<String, Object> params) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'simulate'");
    }

    @Override
    public boolean flush(Map<String, Object> params) {
        // TODO Auto-generated method stub
        String[] orgs=(String[])params.get("orgs");
        String name=String.valueOf(params.get("deploymentName"));
        redisClient.delete(name);
        redisClient.opsForSet().add(name, orgs);
        return true;
    }
    
    public static long getSize(String name) {
        return redisClient.opsForSet().size(name).longValue();
    }
}
