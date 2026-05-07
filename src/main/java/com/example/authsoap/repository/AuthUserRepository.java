package com.example.authsoap.repository;

import com.example.authsoap.model.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/*
 * AuthUserRepository нь auth_user table-тэй харилцах Repository interface юм.
 *
 * Spring Data JPA ашиглаж байгаа тул бид SQL query-г гараар их бичих шаардлагагүй.
 * JpaRepository-г extend хийснээр save, findById, findAll, delete гэх мэт basic DB operation-ууд
 * автоматаар бэлэн болно.
 *
 * Энэ repository-г AuthEndpoint ашиглаж:
 * - register хийх үед username давхардсан эсэхийг шалгана
 * - login хийх үед username-ээр хэрэглэгч хайна
 * - шинэ хэрэглэгчийг database-д хадгална
 */
public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    /*
     * Username-ээр хэрэглэгч хайх method.
     *
     * Spring Data JPA method-ийн нэрээс query-г автоматаар үүсгэнэ.
     * findByUsername гэж нэрлэсэн учраас username column дээр хайлт хийнэ.
     *
     * Optional<AuthUser> ашиглаж байгаа нь тухайн username олдохгүй байж болохыг
     * safe байдлаар илэрхийлж байна.
     *
     * Login хийх үед:
     * - user олдвол password шалгана
     * - user олдохгүй бол Invalid credentials буцаана
     */
    Optional<AuthUser> findByUsername(String username);

    /*
     * Username database дээр аль хэдийн байгаа эсэхийг шалгах method.
     *
     * Register хийх үед нэг username-ээр олон account үүсгэхээс хамгаалахад ашиглана.
     * Хэрэв true буцвал "User already exists" гэж response өгнө.
     */
    boolean existsByUsername(String username);
}