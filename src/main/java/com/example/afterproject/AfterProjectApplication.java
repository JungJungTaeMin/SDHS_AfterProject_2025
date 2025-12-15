package com.example.afterproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // [추가]

@SpringBootApplication
@EnableScheduling
public class AfterProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(AfterProjectApplication.class, args);
    }

}