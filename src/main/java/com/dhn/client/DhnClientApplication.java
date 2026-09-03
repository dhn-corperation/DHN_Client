package com.dhn.client;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAspectJAutoProxy(exposeProxy = true)
public class DhnClientApplication {

	private static ConfigurableApplicationContext context;

	public static void main(String[] args) {
//		SpringApplication.run(DhnClientApplication.class, args);
		context = SpringApplication.run(DhnClientApplication.class, args);
	}

	public static void stop() {
		if (context != null) {
			context.close();
		}
	}

}
