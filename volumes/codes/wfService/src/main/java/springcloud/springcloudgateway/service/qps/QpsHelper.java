package springcloud.springcloudgateway.service.qps;

/**
 * qps统计助手
 * 不会持久化数据
 */

public class QpsHelper {
    private volatile long timestamp = 0;
    private double totalRequest, totalSuccess, totalException, minRt, avgRt, exceptionQps, successQps;
//    private transient volatile Metric rollingStoreInSecond = new ArrayMetric(1, 1000);

    /**
     * 最近1秒的统计信息，将1000毫秒为每1000毫秒统计一次，样本数为1
     */
    private transient volatile Metric rollingCounterInSecond = new ArrayMetric(1, 1000);
    /**
     * 保存最近60秒的统计信息。
     * windowLengthInMs设置为1000毫秒，这意味着每一个bucket对应每秒
     */
    private transient Metric rollingCounterInMinute = new ArrayMetric(60, 60 * 1000);
    private String resource;
    private boolean updated = false;

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public boolean getUpdated() {
        return this.updated;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long tm) {
        timestamp = tm;
    }

    public void setTotalRequest(double totalRequest) {
        this.totalRequest = totalRequest;
    }

    public void setAvgRt(double avgRt) {
        this.avgRt = avgRt;
    }

    public void setExceptionQps(double exceptionQps) {
        this.exceptionQps = exceptionQps;
    }

    public void setMinRt(double minRt) {
        this.minRt = minRt;
    }

    public void setSuccessQps(double successQps) {
        this.successQps = successQps;
    }

    public void setTotalException(double totalException) {
        this.totalException = totalException;
    }

    public void setTotalSuccess(double totalSuccess) {
        this.totalSuccess = totalSuccess;
    }

    public double getTotalRequest() {
        return totalRequest;
    }

    public double getTotalSuccess() {
        return totalSuccess;
    }

    public double getTotalException() {
        return totalException;
    }

    public double getAvgRt() {
        return avgRt;
    }

    public double getMinRt() {
        return minRt;
    }

    public double getExceptionQps() {
        return exceptionQps;
    }

    public double getSuccessQps() {
        return successQps;
    }

    //获取当前窗口时间戳
    public long getTimeInMills() {
        return rollingCounterInSecond.getStartTimeInMills();
    }

    /**
     * 每接收一个请求将请求数自增1并添加耗时
     *
     * @param rt 该请求的耗时（毫秒为单位）
     */
    public void incrSuccess(long rt) {
        rollingCounterInSecond.addSuccess(1);
        rollingCounterInSecond.addRT(rt);
        rollingCounterInMinute.addSuccess(1);
        rollingCounterInMinute.addRT(rt);
    }

    /**
     * 每出现一次异常，将异常总数自增1
     */
    public void incrException() {
        rollingCounterInMinute.addException(1);
        rollingCounterInSecond.addException(1);
    }

    // ====================  分钟为单位的统计 ===============================

    public long totalRequestInMinute() {
        return totalSuccessInMinute() + totalExceptionInMinute();
    }

    public long totalSuccessInMinute() {
        return rollingCounterInMinute.success();
    }

    public long totalExceptionInMinute() {
        return rollingCounterInMinute.exception();
    }

    /**
     * 最小耗时
     *
     * @return
     */
    public double minRtInMinute() {
        return rollingCounterInMinute.minRt();
    }

    /**
     * 成功请求数的平均耗时
     *
     * @return
     */
    public double avgRtInMinute() {
        long successCount = rollingCounterInMinute.success();
        if (successCount == 0) {
            return 0;
        }
        return rollingCounterInMinute.rt() * 1.0 / successCount;
    }

    /**
     * 异常的平均qps
     *
     * @return
     */
    public double exceptionAvgQps() {
        return rollingCounterInMinute.exception() / rollingCounterInMinute.getWindowIntervalInSec();
    }

    /**
     * 成功的平均qps
     *
     * @return
     */
    public double successAvgQps() {
        return rollingCounterInMinute.success() / rollingCounterInMinute.getWindowIntervalInSec();
    }

    // ===================  秒为单位的统计 ==============================

    public long totalRequestInSec() {
        return totalSuccessInSec() + totalExceptionInSec();
    }

    public long totalSuccessInSec() {
        return rollingCounterInSecond.success();
    }

    public long totalExceptionInSec() {
        return rollingCounterInSecond.exception();
    }

    /**
     * 最小耗时
     *
     * @return
     */
    public double minRtInSec() {
        return rollingCounterInSecond.minRt();
    }

    /**
     * 成功请求数的平均耗时
     *
     * @return
     */
    public synchronized double avgRtInSec() {
        long successCount = rollingCounterInSecond.success();
        if (successCount == 0) {
            return 0;
        }
        return rollingCounterInSecond.rt() * 1.0 / successCount;
    }

    /**
     * 异常的qps
     *
     * @return
     */
    public double exceptionQps() {
        return rollingCounterInSecond.exception() / rollingCounterInSecond.getWindowIntervalInSec();
    }

    /**
     * 成功的qps
     *
     * @return
     */
    public double successQps() {
        return rollingCounterInSecond.success() / rollingCounterInSecond.getWindowIntervalInSec();
    }


    /**
     * 获取1分钟的统计
     *
     * @return
     */
    public MetricBucket[] bucketsInMinute() {
        return rollingCounterInMinute.buckets();
    }

    /**
     * 测试数据
     */
    public void printData() {
        System.out.println("Time is :" + rollingCounterInSecond.getStartTime());
        System.out.println("Timemill is :" + rollingCounterInSecond.getStartTimeInMills());
//        System.out.println(this.totalRequestInMinute());
//        System.out.println(this.totalSuccessInMinute());
//        System.out.println(this.totalExceptionInMinute());
//        System.out.println(this.minRtInMinute());
//        System.out.println(this.avgRtInMinute());
//        System.out.println(this.exceptionAvgQps());
//        System.out.println(this.successAvgQps());
        System.out.println(this.totalRequestInSec());
        System.out.println(this.totalSuccessInSec());
        System.out.println(this.totalExceptionInSec());
        System.out.println(this.minRtInSec());
        System.out.println(this.avgRtInSec());
        System.out.println(this.exceptionQps());
        System.out.println(this.successQps());
        System.out.println("------------------");
    }


    public void WriteData(String appName) {
        //json发送至监控节点
//    存入mysql
//        RunableInsert runableInsert=new RunableInsert(appName,rollingCounterInSecond.getStartTime(),this.resource,(int)totalRequestInSec(),(int)totalSuccessInSec(),(int)totalExceptionInSec(),avgRtInSec(),minRtInSec(),(int)successQps(),(int)exceptionQps());
//        Thread thread=new Thread(runableInsert);
//        thread.start();
    }
}
