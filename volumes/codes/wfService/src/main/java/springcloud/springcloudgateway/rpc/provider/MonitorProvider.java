package springcloud.springcloudgateway.rpc.provider;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import springcloud.springcloudgateway.controller.DataController;
import springcloud.springcloudgateway.controller.MonitorController;
import springcloud.springcloudgateway.rpc.remoting.server.ServiceConfig;

@Service
public class MonitorProvider implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        ServiceConfig<DataController> data = new ServiceConfig<>();
        data.setInterface(DataController.class);
        try {
            data.setRef(new MonitorController());
        } catch (Exception e) {
            e.printStackTrace();
        }
        data.export();
    }
}
