package springcloud.springcloudgateway.workflow.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class wfConfig {

    @Value(value= "${workflow.fabric.peersConfigPath}")
    private String peersConfigPath;
    @Value(value= "${workflow.fabric.orderersConfigPath}")
    private String orderersConfigPath;

    @Value(value = "${workflow.flushTimeInterval}")
    private long flushTimeInterval;
    @Value(value = "${workflow.checkResultTimeInterval}")
    private long checkResultTimeInterval;
    @Value(value = "${workflow.checkResultRounds}")
    private int checkResultRounds;
    @Value(value = "${workflow.activitiPort}")
    private String workflowPort;
    @Value(value = "${workflow.fabric.username}")
    private String userName;
    @Value(value = "${workflow.fabric.mspid}")
    private String mspId;
    @Value(value = "${workflow.fabric.keypath}")
    private String keyPath;
    @Value(value = "${workflow.fabric.crtpath}")
    private String crtPath;
    //
    @Value(value = "${workflow.fabric.peertlspath}")
    private String peerTlsPath;
    //
    @Value(value = "${workflow.fabric.peername}")
    private String peerName;
    @Value(value = "${workflow.fabric.peerkeypath}")
    private String peerKeyPath;
    @Value(value = "${workflow.fabric.peercrtpath}")
    private String peerCrtPath;
    //
    @Value(value = "${workflow.fabric.peeraddr}")
    private String peerAddr;
    //
    @Value(value = "${workflow.fabric.orderername}")
    private String ordererName;
    //
    @Value(value = "${workflow.fabric.ordereraddr}")
    private String ordererAddr;
    //
    @Value(value = "${workflow.fabric.orderertlspath}")
    private String ordererTlsPath;
    //
    @Value(value= "${workflow.fabric.autotest}")
    private boolean isTest;

    
    
    

    public String getPeerTlsPath() {
        return peerTlsPath;
    }

    public String getPeersConfigPath() {
        return peersConfigPath;
    }

    public void setPeersConfigPath(String peersConfigPath) {
        this.peersConfigPath = peersConfigPath;
    }

    public String getOrderersConfigPath() {
        return orderersConfigPath;
    }

    public void setOrderersConfigPath(String orderersConfigPath) {
        this.orderersConfigPath = orderersConfigPath;
    }

    public void setPeerTlsPath(String peerTlsPath) {
        this.peerTlsPath = peerTlsPath;
    }

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    public String getPeerKeyPath() {
        return peerKeyPath;
    }

    public void setPeerKeyPath(String peerKeyPath) {
        this.peerKeyPath = peerKeyPath;
    }

    public String getPeerCrtPath() {
        return peerCrtPath;
    }

    public void setPeerCrtPath(String peerCrtPath) {
        this.peerCrtPath = peerCrtPath;
    }

    public String getPeerAddr() {
        return peerAddr;
    }

    public void setPeerAddr(String peerAddr) {
        this.peerAddr = peerAddr;
    }

    public String getOrdererName() {
        return ordererName;
    }

    public void setOrdererName(String ordererName) {
        this.ordererName = ordererName;
    }

    public String getOrdererAddr() {
        return ordererAddr;
    }

    public void setOrdererAddr(String ordererAddr) {
        this.ordererAddr = ordererAddr;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setMspId(String mspId) {
        this.mspId = mspId;
    }

    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }

    public void setCrtPath(String crtPath) {
        this.crtPath = crtPath;
    }
    public long getFlushTimeInterval() {
        return flushTimeInterval;
    }
    public String getUserName() {
        return userName;
    }
    public String getMspId() {
        return mspId;
    }
    public String getKeyPath() {
        return keyPath;
    }

    public void setFlushTimeInterval(long flushTime) {
        this.flushTimeInterval = flushTime;
    }

    public long getCheckResultTimeInterval() {
        return checkResultTimeInterval;
    }

    public void setCheckResultTimeInterval(long checkResultTime) {
        this.checkResultTimeInterval = checkResultTime;
    }

    public int getCheckResultRounds() {
        return checkResultRounds;
    }

    public void setCheckResultRounds(int checkResultRound) {
        this.checkResultRounds = checkResultRound;
    }
    public String getCrtPath() {
        return crtPath;
    }

    public String getWorkflowPort() {
        return workflowPort;
    }

    public void setWorkflowPort(String workflowPort) {
        this.workflowPort = workflowPort;
    }

    public String getOrdererTlsPath() {
        return ordererTlsPath;
    }

    public void setOrdererTlsPath(String ordererTlsPath) {
        this.ordererTlsPath = ordererTlsPath;
    }

    public boolean isTest() {
        return isTest;
    }

    public void setTest(boolean isTest) {
        this.isTest = isTest;
    }
    
}
