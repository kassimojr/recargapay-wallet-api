package com.recargapay.wallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RecargapayWalletApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecargapayWalletApiApplication.class, args);
	}

}
