package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.example.demo", "com.example.authsoap"})
@EntityScan(basePackages = "com.example.authsoap.model")
@EnableJpaRepositories(basePackages = "com.example.authsoap.repository")
public class UserSoapServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserSoapServiceApplication.class, args);
    }
}
