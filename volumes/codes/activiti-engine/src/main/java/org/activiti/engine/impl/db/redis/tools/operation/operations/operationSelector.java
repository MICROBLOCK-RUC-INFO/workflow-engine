package org.activiti.engine.impl.db.redis.tools.operation.operations;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.db.redis.tools.operation.handlers.deployHandler;
import org.activiti.engine.impl.db.redis.tools.operation.handlers.handler;
import org.activiti.engine.impl.db.redis.tools.operation.handlers.registryHandler;
import org.activiti.engine.impl.db.redis.tools.operation.handlers.serviceTaskBindHandler;
import org.activiti.engine.impl.db.redis.tools.operation.handlers.userTaskBindHandler;
import org.activiti.engine.impl.db.redis.tools.operation.handlers.verifyHandler;

/**
 * @apiNote handler选择器
 * 这是比较后期写的代码，当时是想把对工作流引擎的所有操作都封装成一个operation
 * 然后根据operation的类型在选择器中选择对应的handler
 * 每个handler都需要实现simulate和flush两个功能函数，其中的逻辑根据operation不同自行编写
 * 当时想把deploy,instance,complete都集成进来，但是没来得及
 * ps:其实handler有点多余，可以直接在operation的接口里定义simultate和flush两个函数，就跟命令模式一样。但是当时我没有想到那么多，现在想来命令模式更具有可实现性
 */
public class operationSelector {
    //用的是直接初始化的方式，如果有扩展的记得增加
    final Map<operation.oType,handler> selector=new HashMap<operation.oType,handler>() {{
        put(operation.oType.userTaskBind,new userTaskBindHandler());
        put(operation.oType.serviceTaskBind,new serviceTaskBindHandler());
        put(operation.oType.userRegistry,new registryHandler());
        put(operation.oType.verify,new verifyHandler());
        put(operation.oType.deploy,new deployHandler());
    }};

    /**
     * @apiNote 模拟执行
     */
    public String simulateHandle(operation o) {
        return selector.get(o.getoType()).simulate(o.getParams());
    }

    /**
     * @apiNote flush
     */
    public boolean flushHandle(operation o) {
        return selector.get(o.getoType()).flush(o.getParams());
    }


    public userTaskBindHandler getUserTaskBindHandler() {
        return (userTaskBindHandler)selector.get(operation.oType.userTaskBind);
    }

    public serviceTaskBindHandler getServiceTaskBindHandler() {
        return (serviceTaskBindHandler)selector.get(operation.oType.serviceTaskBind);
    }
}
