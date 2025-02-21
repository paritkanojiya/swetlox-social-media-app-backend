package com.swetlox_app.swetlox;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableMongoAuditing
public class SwetloxApplication {


	public static void main(String[] args) {

		SpringApplication.run(SwetloxApplication.class, args);
	}
}
