package com.digital.wallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DigitalWalletApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(DigitalWalletApiApplication.class, args);
	}

}
