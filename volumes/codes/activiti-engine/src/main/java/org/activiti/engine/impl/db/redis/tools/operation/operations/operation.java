package org.activiti.engine.impl.db.redis.tools.operation.operations;

import java.util.Map;

public class operation {
    public static enum oType {
        userTaskBind,serviceTaskBind,
        userRegistry,verify,deploy;        
    }

    private oType oType;
    private Map<String,Object> params;


    public oType getoType() {
        return oType;
    }
    public void setoType(oType oType) {
        this.oType = oType;
    }
    public Map<String, Object> getParams() {
        return params;
    }
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
