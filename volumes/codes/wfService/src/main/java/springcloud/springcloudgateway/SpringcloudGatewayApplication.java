package springcloud.springcloudgateway;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringcloudGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringcloudGatewayApplication.class, args);
	}

}

// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.cache.annotation.EnableCaching;
// import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.ComponentScan;
// import org.springframework.context.annotation.FilterType;
// import org.springframework.scheduling.annotation.EnableAsync;
// import org.springframework.web.client.RestTemplate;
// import springcloud.springcloudgateway.config.ExcludeFromComponentScan;


// @ComponentScan(excludeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, value = ExcludeFromComponentScan.class)})
// @EnableDiscoveryClient
// @SpringBootApplication
// @EnableCaching
// @EnableAsync
// public class SpringcloudGatewayApplication {
//     @Bean
//     public RestTemplate restTemplate() {
//         return new RestTemplate();
//     }

//     public static void main(String[] args) {
//         try {
//             SpringApplication.run(SpringcloudGatewayApplication.class, args);
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }

// }
