// package springcloud.springcloudgateway.config;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.boot.ApplicationArguments;
// import org.springframework.boot.ApplicationRunner;
// import org.springframework.cloud.alibaba.nacos.registry.NacosAutoServiceRegistration;
// import org.springframework.stereotype.Component;

// @Component
// public class NacosConfig implements ApplicationRunner {

//     @Autowired(required = false)
//     private NacosAutoServiceRegistration registration;

//     @Value("${server.port}")
//     Integer port;

//     Integer tomcatPort;

//     @Override
//     public void run(ApplicationArguments args) {
//         if (registration != null && port != null) {
//             tomcatPort = port;
//             registration.setPort(tomcatPort);
//             registration.start();
//         }
//     }

//     /**
//      *	获取外部tomcat端口
//      */
// }