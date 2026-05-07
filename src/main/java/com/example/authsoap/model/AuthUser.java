package com.example.authsoap.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
 * AuthUser нь SOAP authentication service-ийн хэрэглэгчийг илэрхийлэх Entity class юм.
 *
 * Энэ class нь PostgreSQL database дээрх "auth_user" table-тэй холбогдоно.
 * Өөрөөр хэлбэл энэ class-ийн object бүр database дээр нэг row болж хадгалагдана.
 *
 * Энэ entity-г нэмснээр username/password memory дээр биш database дээр хадгалагдаж,
 * service restart хийсэн ч register хийсэн хэрэглэгч алга болохгүй болсон.
 */
@Entity
@Table(name = "auth_user")
public class AuthUser {

    /*
     * id нь database table-ийн primary key.
     *
     * GenerationType.IDENTITY ашигласнаар PostgreSQL өөрөө id-г автоматаар нэмэгдүүлж үүсгэнэ.
     * Жишээ нь эхний user id=1, дараагийн user id=2 гэх мэт.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * username талбар.
     *
     * nullable = false гэдэг нь username заавал байх ёстой гэсэн үг.
     * unique = true гэдэг нь нэг username-ээр хоёр өөр user бүртгүүлэх боломжгүй гэсэн үг.
     *
     * Энэ нь register хийх үед duplicate хэрэглэгч үүсэхээс хамгаална.
     */
    @Column(nullable = false, unique = true)
    private String username;

    /*
     * password талбар.
     *
     * nullable = false гэдэг нь password хоосон байж болохгүй гэсэн үг.
     *
     * Энэ lab-ийн хувьд password-ийг энгийн text байдлаар хадгалж байгаа.
     * Production системд бол password-ийг BCrypt зэрэг алгоритмаар hash хийж хадгалах ёстой.
     */
    @Column(nullable = false)
    private String password;

    /*
     * Default constructor.
     *
     * JPA/Hibernate entity object үүсгэхдээ заавал хоосон constructor шаарддаг.
     * Тиймээс энэ constructor-ийг үлдээх хэрэгтэй.
     */
    public AuthUser() {
    }

    /*
     * Username болон password авч шинэ AuthUser object үүсгэх constructor.
     *
     * RegisterUser operation дээр шинэ хэрэглэгч үүсгэхдээ энэ constructor-ийг ашиглаж байна.
     */
    public AuthUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /*
     * id getter.
     *
     * Database-д хадгалагдсаны дараа PostgreSQL-ээс автоматаар үүссэн id-г авахад ашиглаж болно.
     */
    public Long getId() {
        return id;
    }

    /*
     * username getter.
     *
     * Login хийх үед database-аас олдсон хэрэглэгчийн username-г унших боломжтой.
     */
    public String getUsername() {
        return username;
    }

    /*
     * username setter.
     *
     * JPA object үүсгэх, update хийх үед username утгыг онооход ашиглагдана.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /*
     * password getter.
     *
     * Login хийх үед хэрэглэгчийн оруулсан password-ийг database дээр хадгалагдсан password-той
     * харьцуулахад ашиглаж байна.
     */
    public String getPassword() {
        return password;
    }

    /*
     * password setter.
     *
     * JPA object үүсгэх эсвэл password утгыг өөрчлөх үед ашиглаж болно.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}