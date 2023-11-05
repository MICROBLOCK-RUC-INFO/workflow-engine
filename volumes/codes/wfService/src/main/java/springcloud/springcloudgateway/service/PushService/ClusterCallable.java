package springcloud.springcloudgateway.service.PushService;

import jep.Interpreter;

import java.util.concurrent.Callable;

/**
 * @Author: 李浩然
 * @Date: 2021/10/11 6:15 下午
 */
class ClusterCallable implements Callable {
    private double[][] array;
    private Interpreter interp;

    public ClusterCallable(Interpreter interp, double[][] array) {
        this.interp = interp;
        this.array = array;
    }

    @Override
    public Object call() {
        interp.set("array", array);
        return interp.getValue("getClusters(array)");
    }
}
