package org.activiti.engine.impl.db.redis.tools.operation.operations;

import java.util.Map;

public class operationBuilder {
    private operation operation;
    public operationBuilder() {
        this.operation=new operation();
    }
    
    public operationBuilder setType(operation.oType type) {
        operation.setoType(type);
        return this;
    }

    public operationBuilder setParams(Map<String,Object> params) {
        operation.setParams(params);
        return this;
    }

    public operation build() {
        return operation;
    }
}
