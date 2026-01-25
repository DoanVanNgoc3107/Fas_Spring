package com.example.fas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FasApplication {

    public static void main(String[] args) {
        SpringApplication.run(FasApplication.class, args);
    }

}
