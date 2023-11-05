// package springcloud.springcloudgateway.config;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.context.event.ApplicationReadyEvent;
// import org.springframework.cloud.alibaba.nacos.registry.NacosAutoServiceRegistration;
// import org.springframework.cloud.alibaba.nacos.registry.NacosRegistration;
// import org.springframework.context.ApplicationListener;
// import org.springframework.context.event.EventListener;
// import org.springframework.stereotype.Component;

// @Component
// public class NacosListener implements ApplicationListener<ApplicationReadyEvent> {


//     @Autowired
//     private NacosRegistration registration;

//     @Autowired
//     private NacosAutoServiceRegistration nacosAutoServiceRegistration;

//     @Override
//     public void onApplicationEvent(ApplicationReadyEvent event) {
//         String property = event.getApplicationContext().getEnvironment().getProperty("server.port");
//         registration.setPort(Integer.valueOf(property));
//         nacosAutoServiceRegistration.start();
//     }

//     @EventListener(ApplicationReadyEvent.class)
//     public void onWebServerReady(ApplicationReadyEvent event) {
//         String property = event.getApplicationContext().getEnvironment().getProperty("server.port");
//         registration.setPort(Integer.valueOf(property));
//         nacosAutoServiceRegistration.start();
//     }
// }
