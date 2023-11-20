package com.wq.wfEngine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class WfEngineApplication {
	public static ConfigurableApplicationContext context;
	public static void main(String[] args) {
		context=SpringApplication.run(WfEngineApplication.class, args);
	}

}
