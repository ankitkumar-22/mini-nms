package com.nms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(NmsApplication.class, args);
    }
}