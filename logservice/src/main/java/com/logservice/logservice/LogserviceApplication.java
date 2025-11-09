package com.logservice.logservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LogserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogserviceApplication.class, args);
    }
}
