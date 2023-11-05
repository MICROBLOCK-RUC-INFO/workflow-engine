package springcloud.springcloudgateway.service.qps;

import java.sql.SQLException;
import java.sql.Timestamp;

public class RunableInsert implements Runnable {
    private String app;
    private Timestamp clickTime;
    private Timestamp blockchainTime;
    private Timestamp serviceTime;
    private String resource;
    private int totalRequest, totalSuccess, totalException, success_qps, exception_qps;
    private double rt_avg, rt_min;
    private String ipaddr;
    private String service;
    private String port;
    private String host;
    private int delay;
    private int serviceDelay;
    private int blockchainDelay;
    private String serivceCompleted;
    private String blockchainCompleted;
    private int nums;
    private long clickTimeInt;

    public RunableInsert(Timestamp clickTime, Timestamp serviceTime, Timestamp blockchainTime, long clickTimeInt, String service, String ipaddr, String host, String port, int serviceDelay, int blockchainDelay, int delay, int nums, String serviceCompleted, String blockchainCompleted) {
        this.service = service;
        this.ipaddr = ipaddr;
        this.clickTime = clickTime;
        this.blockchainTime = blockchainTime;
        this.serviceTime = serviceTime;
        this.port = port;
        this.host = host;
        this.clickTimeInt = clickTimeInt;
        this.delay = delay;
        this.serviceDelay = serviceDelay;
        this.blockchainDelay = blockchainDelay;
        this.nums = nums;
        this.serivceCompleted = serviceCompleted;
        this.blockchainCompleted = blockchainCompleted;
    }

    //鐩戞帶鏁版嵁鎻掑叆mysql
//    public RunableInsert(String app,Timestamp tm,String resource ,int totalRequest,int totalSuccess,int totalException,double rt_avg,double rt_min,int success_qps,int exception_qps) {
//        this.app=app;
//        this.tm=tm;
//        this.resource = resource;
//        this.totalRequest=totalRequest;
//        this.totalSuccess=totalSuccess;
//        this.totalException=totalException;
//        this.rt_avg=rt_avg;
//        this.rt_min=rt_min;
//        this.success_qps=success_qps;
//        this.exception_qps=exception_qps;
//    }
    public void run() {
//        Consistency.insert(app,tm,resource,totalRequest,totalSuccess,totalException,rt_avg,rt_min,success_qps,exception_qps);
        try {
            Consistency.insertMonitorData(clickTime, serviceTime, blockchainTime, clickTimeInt, service, ipaddr, host, port, serviceDelay, blockchainDelay, delay, nums, serivceCompleted, blockchainCompleted);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}