package com.wq.wfEngine.tool;

import java.util.HashMap;
import java.util.Map;

public class jsonTreeNode {
    public String jsonStr;
    public Map<String,jsonTreeNode> sons;
    public jsonTreeNode(String jsonStr) {
        this.jsonStr=jsonStr;
        this.sons=new HashMap<>();
    }
    public jsonTreeNode(String jsonStr,Map<String,jsonTreeNode> sons) {
        this.jsonStr=jsonStr;
        this.sons=sons;
    } 
}
