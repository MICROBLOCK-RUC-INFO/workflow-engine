package springcloud.springcloudgateway.rpc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DemoServiceImpl implements DemoService {
    private static final Logger logger = LoggerFactory.getLogger(DemoServiceImpl.class);

    @Override
    public String sayHello(String name) {
        logger.info("Hello " + name + ", request from consumer");
        return "Hello " + name + ", response from provider";
    }

    @Override
    public String sayOneTwoThree(String name) {
        logger.info(name + ", request from consumer");
        return name + " syaid: 1,2,3." + ", response from provider";
    }
}
