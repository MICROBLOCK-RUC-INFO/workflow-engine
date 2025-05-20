package com.wq.wfEngine.cache;

import java.util.Arrays;
import java.util.List;

import com.wq.wfEngine.tool.read;

/**
 * @apiNote 这个暂时没用了，当时是要通过监控链调用服务，然后才写了这个，轮流监控节点调用服务
 */
public class monitorIps {
    private static List<String> monitorIps;
    
    public static void initMonitorIps () {
        String filePath=System.getenv("MONITOR_CONFIG");
        String fileContent=read.readFile(filePath);
        monitorIps=Arrays.asList(fileContent.substring(1, fileContent.length()-1).split(","));
    }

    public static List<String> getMonitorIps() {
        return monitorIps;
    }

    public static int getMonitorAmount() {
        return monitorIps.size();
    }

    public static int getInitialMonitorNumber(int hashCode) {
        return hashCode%monitorIps.size();
    }

    public static String chooseMonitorIp(int number) {
        return monitorIps.get(number);
    }
}
