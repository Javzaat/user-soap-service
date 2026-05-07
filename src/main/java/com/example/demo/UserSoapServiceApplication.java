package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/*
 * UserSoapServiceApplication нь SOAP Authentication Service-ийн main class юм.
 *
 * Spring Boot application яг энэ class-аас эхэлж ажиллана.
 * Энэ service-ийн гол үүрэг нь:
 * - хэрэглэгч register хийх
 * - login хийх
 * - token үүсгэх
 * - token validate хийх
 *
 * Lab 06 дээр JSON service нь authentication logic-ийг өөр дээрээ хийхгүй,
 * харин энэ SOAP service рүү token validation request илгээдэг.
 */
@SpringBootApplication(scanBasePackages = {"com.example.demo", "com.example.authsoap"})

/*
 * EntityScan нь JPA entity class-уудыг хаанаас хайхыг Spring Boot-д зааж өгч байна.
 *
 * Манай AuthUser entity нь:
 * com.example.authsoap.model
 *
 * package дотор байгаа. Main class өөрөө com.example.demo package-д байгаа учраас
 * default scan-аар AuthUser entity-г олохгүй байж болно. Тиймээс энд entity scan package-ийг
 * тодорхой зааж өгсөн.
 */
@EntityScan(basePackages = "com.example.authsoap.model")

/*
 * EnableJpaRepositories нь Spring Data JPA repository interface-үүдийг хаанаас хайхыг заана.
 *
 * Манай AuthUserRepository нь:
 * com.example.authsoap.repository
 *
 * package дотор байгаа. Энэ annotation байхгүй үед Spring Boot repository-г bean болгож үүсгэхгүй,
 * AuthEndpoint дээр AuthUserRepository inject хийх үед error гарч байсан.
 */
@EnableJpaRepositories(basePackages = "com.example.authsoap.repository")
public class UserSoapServiceApplication {

    /*
     * main method нь Spring Boot application-ийг эхлүүлнэ.
     *
     * Энэ method ажиллах үед:
     * - Spring context үүснэ
     * - SOAP endpoint-ууд бүртгэгдэнэ
     * - JPA entity болон repository scan хийгдэнэ
     * - PostgreSQL database connection үүснэ
     * - Tomcat server 8081 port дээр асна
     */
    public static void main(String[] args) {
        SpringApplication.run(UserSoapServiceApplication.class, args);
    }
}