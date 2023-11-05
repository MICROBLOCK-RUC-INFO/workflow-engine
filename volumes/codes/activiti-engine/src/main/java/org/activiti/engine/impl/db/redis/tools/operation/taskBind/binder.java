package org.activiti.engine.impl.db.redis.tools.operation.taskBind;

import java.util.Map;

public interface binder {
    public static void staticAllocate(String oid,Map<String,Object> staticAllocation) {};
}
