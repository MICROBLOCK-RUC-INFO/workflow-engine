package com.wq.wfEngine.pojo;

public class flowNode {
    private String nextNode;
    private String key;
    private String value;
    public String getNextNode() {
        return nextNode;
    }
    public void setNextNode(String nextNode) {
        this.nextNode = nextNode;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    } 
}
