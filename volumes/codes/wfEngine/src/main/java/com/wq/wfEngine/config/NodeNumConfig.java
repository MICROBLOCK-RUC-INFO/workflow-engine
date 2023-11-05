package com.wq.wfEngine.config;

import java.util.Map;

public class NodeNumConfig {
    private static int thisNodeNum;
    private static int allNodeNum;
    
    public static void initNodeInfo() {
        Map<String,String> sysEnv=System.getenv();
        thisNodeNum=Integer.valueOf(sysEnv.get("nodeNum")).intValue();
        allNodeNum=Integer.valueOf(sysEnv.get("nodeAmount")).intValue();
    }

    public static int getThisNodeNum() {
        return thisNodeNum;
    }


    public static int getAllNodeNum() {
        return allNodeNum;
    }
    
}
