package com.itgrail.midas.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ConsumerOldApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConsumerOldApplication.class, args);
	}
}