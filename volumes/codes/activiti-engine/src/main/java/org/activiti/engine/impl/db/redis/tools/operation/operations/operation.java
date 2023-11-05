package org.activiti.engine.impl.db.redis.tools.operation.operations;

import java.util.Map;

public class operation {
    //暂时只有taskBind,有需要再添加
    public static enum oType {
        userTaskBind,serviceTaskBind;        
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
