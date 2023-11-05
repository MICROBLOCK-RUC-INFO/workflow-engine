package org.activiti.engine.impl.db.redis.tools.operation.operations;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.db.redis.tools.operation.handlers.handler;
import org.activiti.engine.impl.db.redis.tools.operation.handlers.serviceTaskBindHandler;
import org.activiti.engine.impl.db.redis.tools.operation.handlers.userTaskBindHandler;


public class operationSelector {
    //用的是直接初始化的方式，如果有扩展的记得增加
    final Map<operation.oType,handler> selector=new HashMap<operation.oType,handler>() {{
        put(operation.oType.userTaskBind,new userTaskBindHandler());
        put(operation.oType.serviceTaskBind,new serviceTaskBindHandler());
    }};

    public String simulateHandle(operation o) {
        return selector.get(o.getoType()).simulate(o.getParams());
    }

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
