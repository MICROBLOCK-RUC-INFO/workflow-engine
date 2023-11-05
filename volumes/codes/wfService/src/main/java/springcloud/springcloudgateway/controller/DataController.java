package springcloud.springcloudgateway.controller;


/**
 * @Author: 李浩然
 * @Date: 2021/1/8 11:25 下午
 */
public interface DataController {
    String run(String req, String loadBalance) throws Exception;
}
