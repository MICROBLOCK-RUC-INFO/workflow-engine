package org.activiti.engine.impl.db.redis.tools.rwSet;

import org.activiti.engine.impl.db.workflowClass.rwSetClass.simpleEntity;
import org.activiti.engine.impl.db.workflowClass.rwSetClass.simpleTask;
import org.activiti.engine.impl.persistence.entity.Entity;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;

//工厂模式
public class entityGenerator {
    private Entity generator(simpleEntity simpleEntity) {
        return null;
    }

    private TaskEntityImpl taskGenerator(simpleTask simpleTask) {
        TaskEntityImpl task=new TaskEntityImpl();
        task.setOid(simpleTask.getOid());
        simpleTask.getUserId();
        return null;
    }
}
