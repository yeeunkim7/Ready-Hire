package com.devinterview.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DevInterviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevInterviewApplication.class, args);
    }
}
