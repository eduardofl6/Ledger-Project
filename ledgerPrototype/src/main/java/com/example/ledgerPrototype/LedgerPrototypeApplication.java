package com.example.ledgerPrototype;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LedgerPrototypeApplication {

	public static void main(String[] args) {
		SpringApplication.run(LedgerPrototypeApplication.class, args);
	}

}
