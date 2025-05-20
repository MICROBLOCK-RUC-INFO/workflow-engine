package org.activiti.engine.impl.db.redis.tools.operation.handlers;

import java.util.Map;

import org.activiti.engine.impl.db.redis.tools.operation.registry.registerOperator;

/**
 * @apiNote 组织(Org)注册,虽然日志里写的是用户
 */
public class registryHandler implements handler{

    @Override
    public String simulate(Map<String, Object> params) {
        return null;
    }

    @Override
    public boolean flush(Map<String, Object> params) {
        String name=String.valueOf(params.get("name"));
        String oldPrivateKey=registerOperator.isRegistered(name);
        if (oldPrivateKey!=null&&!params.containsKey("oldPrivateKey")) {
            throw new RuntimeException(String.format("用户%s注册失败,因为该用户已注册，需要提供原私钥，http请求中需要提供oldPrivateKey", name));
        }
        if (oldPrivateKey!=null&&!oldPrivateKey.equals(String.valueOf(params.get("oldPrivateKey")))) {
            throw new RuntimeException(String.format("用户%s注册失败，因为该用户已注册，且提供的原私钥不符", name));
        }
        String privateKey=String.valueOf(params.get("privateKey"));
        String publicKey=String.valueOf(params.get("publicKey"));
        registerOperator.register(name, publicKey, privateKey);
        return true;
    }
    
}
