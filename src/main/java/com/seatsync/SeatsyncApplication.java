package com.seatsync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SeatsyncApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeatsyncApplication.class, args);
    }
}