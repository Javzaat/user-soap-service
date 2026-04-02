package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example.demo", "com.example.authsoap"})
public class UserSoapServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserSoapServiceApplication.class, args);
    }
}
