package org.activiti.engine.impl.db.redis.timerTask;

import java.util.Timer;
import java.util.TimerTask;

import org.activiti.engine.impl.db.redis.personalConfig;
import org.activiti.engine.impl.db.redis.tools.operation.taskBind.tableOperator;

public class loopCleanCache {
    public void start() {
        Timer timer=new Timer();
        timer.schedule(new cleaner(personalConfig.getCacheCleanTime()), 0L, personalConfig.getCacheCleanTime());
    }
    class cleaner extends TimerTask {
        long maxTime;
        public cleaner(long maxTime) {
            this.maxTime=maxTime;
        }
        @Override
        public void run() {
            tableOperator.cleanCache(maxTime);
        }
        
    }
}
