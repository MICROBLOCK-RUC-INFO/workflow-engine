package org.activiti.engine.impl.db.redis.tools.operation.handlers;

import java.util.Map;

public interface handler {
    /**
     * @apiNote 模拟执行
     * @param params 模拟执行的参数，以map的形式传入
     * @return 执行后得到的读写集的json字符串
     */
    public String simulate(Map<String,Object> params);
    /**
     * @apiNote 真实执行，由读写集来改变状态数据库状态
     * @param params 真实执行的参数，以map的形式传入，由读写集转换而来
     * @return 执行成功返回true,执行失败返回false
     */
    public boolean flush(Map<String,Object> params);
}
