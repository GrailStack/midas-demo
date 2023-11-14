package com.itgrail.midas.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.itgrail.midas.demo.service")
public class ConsumerNewApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConsumerNewApplication.class, args);
	}
}