package org.activiti.engine.impl.db.redis.tools.operation.handlers;

import java.util.Map;

import org.activiti.engine.impl.db.redis.tools.operation.verify.verifyOperator;

/**
 * @apiNote 签名校验
 */
public class verifyHandler implements handler{

    @Override
    public String simulate(Map<String, Object> params) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'simulate'");
    }

    /**
     * @apiNote 校验不需要模拟执行，或许应该集成在其他操作的模拟执行里，但是要改的太多了
     */
    @Override
    public boolean flush(Map<String, Object> params) {
        // TODO Auto-generated method stub
        String name=String.valueOf(params.get("name"));
        String data=String.valueOf(params.get("data"));
        String signature=String.valueOf(params.get("signature"));
        return verifyOperator.verify(name, data, signature);
    }
    
}
