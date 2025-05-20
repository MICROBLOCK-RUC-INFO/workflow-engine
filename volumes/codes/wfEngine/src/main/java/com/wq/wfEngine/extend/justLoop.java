package com.wq.wfEngine.extend;

import java.util.Timer;
import java.util.TimerTask;

import com.wq.wfEngine.activiti.workflowFunction;

/**
 * @apiNote 简单的定时任务，因为mybatis数据库连接池的问题所以用的这种方法
 */
public class justLoop {

    public void start() {
        Timer timer=new Timer();
        timer.schedule(new loopDb(), 0, 1*60*60*1000);
    }

    class loopDb extends TimerTask {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            workflowFunction.getAllDeployments();
            workflowFunction.loop();
        }
        
    }
}
