package com.wq.wfEngine.tool.serviceComposition;

import java.util.Map;

public class routeTransfer {
    public String transfer(String route,Map<String,Object> dataRoot) {
        int index=route.indexOf("?");
        if (index<0) return route;
        String[] keyValues=route.substring(index).split("&");
        StringBuilder sBuilder=new StringBuilder().append(route.substring(0, index+1));
        for (String keyValue:keyValues) {
            String[] strs=keyValue.split("=");
        }
        return null;
    }
}
