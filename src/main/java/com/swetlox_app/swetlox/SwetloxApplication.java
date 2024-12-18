package com.swetlox_app.swetlox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SwetloxApplication {

	public static void main(String[] args) {
		SpringApplication.run(SwetloxApplication.class, args);
	}

}
