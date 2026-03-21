package com.bharatbloodbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BharatBloodBankApplication {
    public static void main(String[] args) {
        SpringApplication.run(BharatBloodBankApplication.class, args);
    }
}
