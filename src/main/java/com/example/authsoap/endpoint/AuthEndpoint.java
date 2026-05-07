package com.example.authsoap.endpoint;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.example.authsoap.LoginUserRequest;
import com.example.authsoap.LoginUserResponse;
import com.example.authsoap.RegisterUserRequest;
import com.example.authsoap.RegisterUserResponse;
import com.example.authsoap.ValidateTokenRequest;
import com.example.authsoap.ValidateTokenResponse;
import com.example.authsoap.model.AuthUser;
import com.example.authsoap.repository.AuthUserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/*
 * AuthEndpoint нь SOAP Authentication Service-ийн үндсэн endpoint class юм.
 *
 * Энэ class нь Lab 06 дээр шаардсан authentication logic-ийг хариуцна:
 * - RegisterUser
 * - LoginUser
 * - ValidateToken
 *
 * Frontend register/login хийх үед API Gateway-ээр дамжаад энэ SOAP service рүү request ирнэ.
 * Харин JSON service дээр profile CRUD хийх үед ирсэн token-ийг баталгаажуулахын тулд
 * JSON service мөн энэ SOAP service-ийн ValidateToken operation-ийг дууддаг.
 */
@Endpoint
public class AuthEndpoint {

    /*
     * SOAP request-ийн namespace.
     * XSD schema дээр тодорхойлсон namespace-тэй яг адил байх ёстой.
     * Spring WS энэ namespace болон localPart-ийг ашиглаад аль method ажиллахыг шийддэг.
     */
    private static final String NAMESPACE_URI = "http://example.com/authsoap";

    /*
     * AuthUserRepository нь PostgreSQL database дахь auth_user table-тэй харилцана.
     *
     * Өмнөх хувилбарт username/password нь HashMap memory дээр хадгалагдаж байсан.
     * Тийм үед service restart хийхэд бүх registered user алга болдог байсан.
     *
     * Одоо repository ашигласнаар register хийсэн user PostgreSQL дээр хадгалагдана.
     * Ингэснээр SOAP service restart хийсэн ч хэрэглэгч дахин register хийх шаардлагагүй.
     */
    private final AuthUserRepository authUserRepository;

    /*
     * Token-уудыг одоогоор memory дээр түр хадгалж байна.
     *
     * Энэ lab-ийн хувьд username/password persistent буюу DB дээр хадгалагдах нь гол засвар.
     * Token memory дээр хадгалагдаж байгаа тул SOAP service restart хийвэл өмнөх token хүчингүй болно.
     * Гэхдээ user DB дээр байгаа учраас хэрэглэгч дахин login хийж шинэ token авч чадна.
     *
     * Map-ийн key нь token, value нь тухайн token-ийг авсан username.
     */
    private final Map<String, String> tokens = new HashMap<>();

    /*
     * Constructor injection.
     *
     * Spring Boot application асах үед AuthUserRepository bean-ийг автоматаар үүсгээд
     * энэ constructor-оор AuthEndpoint руу inject хийдэг.
     */
    public AuthEndpoint(AuthUserRepository authUserRepository) {
        this.authUserRepository = authUserRepository;
    }

    /*
     * RegisterUser SOAP operation.
     *
     * Frontend register page-ээс username/password илгээхэд энэ method ажиллана.
     * Method-ийн зорилго:
     * 1. Username/password хоосон эсэхийг шалгах
     * 2. Тухайн username өмнө бүртгэгдсэн эсэхийг DB-ээс шалгах
     * 3. Байхгүй бол auth_user table-д шинэ user хадгалах
     * 4. SOAP response message буцаах
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "RegisterUserRequest")
    @ResponsePayload
    public RegisterUserResponse registerUser(@RequestPayload RegisterUserRequest request) {

        /*
         * SOAP response object үүсгэж байна.
         * Энэ object дээр message set хийгээд frontend рүү буцаана.
         */
        RegisterUserResponse response = new RegisterUserResponse();

        /*
         * SOAP request body-оос username болон password авч байна.
         */
        String username = request.getUsername();
        String password = request.getPassword();

        /*
         * Username эсвэл password хоосон байвал register хийхгүй.
         * Энэ нь database-д буруу/хоосон user орохоос хамгаална.
         */
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            response.setMessage("Username or password cannot be empty");
            return response;
        }

        /*
         * Username-ийн эхлэл/төгсгөлийн хоосон зайг арилгаж байна.
         * Жишээ нь " testuser " гэж орж ирвэл "testuser" болгож хадгална.
         */
        username = username.trim();

        /*
         * Ижил username өмнө бүртгэгдсэн эсэхийг PostgreSQL DB-ээс шалгаж байна.
         * Username unique байх ёстой тул давхар user үүсгэхгүй.
         */
        if (authUserRepository.existsByUsername(username)) {
            response.setMessage("User already exists");
            return response;
        }

        /*
         * Шинэ AuthUser entity үүсгээд database-д хадгалж байна.
         * Энэ үед auth_user table-д username/password record болж орно.
         */
        AuthUser user = new AuthUser(username, password);
        authUserRepository.save(user);

        /*
         * Register амжилттай болсон response.
         */
        response.setMessage("User registered successfully");
        return response;
    }

    /*
     * LoginUser SOAP operation.
     *
     * Frontend login page-ээс username/password илгээхэд энэ method ажиллана.
     * Method-ийн зорилго:
     * 1. Username/password хоосон эсэхийг шалгах
     * 2. Username-ийг DB-ээс хайх
     * 3. DB дээр байгаа password request-ийн password-той таарч байгаа эсэхийг шалгах
     * 4. Таарвал token үүсгээд frontend рүү буцаах
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "LoginUserRequest")
    @ResponsePayload
    public LoginUserResponse loginUser(@RequestPayload LoginUserRequest request) {

        /*
         * Login response object үүсгэж байна.
         * Амжилттай үед token болон message буцаана.
         * Амжилтгүй үед empty token болон error message буцаана.
         */
        LoginUserResponse response = new LoginUserResponse();

        /*
         * SOAP request body-оос username/password авч байна.
         */
        String username = request.getUsername();
        String password = request.getPassword();

        /*
         * Хоосон username/password орж ирвэл шууд invalid гэж үзнэ.
         */
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            response.setToken("");
            response.setMessage("Invalid credentials");
            return response;
        }

        /*
         * Username-ийн илүү хоосон зайг арилгаж байна.
         */
        username = username.trim();

        /*
         * PostgreSQL database-аас username-ээр user хайж байна.
         * Optional ашиглаж байгаа нь user олдохгүй байж болох тохиолдлыг safe handle хийхэд хэрэгтэй.
         */
        Optional<AuthUser> userOptional = authUserRepository.findByUsername(username);

        /*
         * User олдсон бөгөөд DB дээр хадгалагдсан password нь request password-той таарвал
         * login амжилттай гэж үзнэ.
         */
        if (userOptional.isPresent() && userOptional.get().getPassword().equals(password)) {

            /*
             * UUID ашиглаж random token үүсгэж байна.
             * Энэ token нь дараагийн JSON/File API request-үүд дээр Authorization header-аар явна.
             */
            String token = UUID.randomUUID().toString();

            /*
             * Үүссэн token-ийг memory Map-д хадгалж байна.
             * Дараа нь ValidateToken operation энэ Map дээр token байгаа эсэхийг шалгана.
             */
            tokens.put(token, username);

            /*
             * Token болон амжилттай message-ийг frontend рүү буцааж байна.
             */
            response.setToken(token);
            response.setMessage("Login successful");
        } else {

            /*
             * Username олдоогүй эсвэл password буруу үед invalid credentials буцаана.
             */
            response.setToken("");
            response.setMessage("Invalid credentials");
        }

        return response;
    }

    /*
     * ValidateToken SOAP operation.
     *
     * JSON service болон File Manager service нь protected request ирэх үед
     * token зөв эсэхийг энэ operation-оор шалгуулна.
     *
     * Жишээ:
     * Frontend -> JSON Service -> SOAP ValidateToken
     *
     * Token valid бол JSON service profile CRUD request-ийг үргэлжлүүлнэ.
     * Token invalid бол 401 Unauthorized буцаана.
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "ValidateTokenRequest")
    @ResponsePayload
    public ValidateTokenResponse validateToken(@RequestPayload ValidateTokenRequest request) {

        /*
         * ValidateToken response object үүсгэж байна.
         */
        ValidateTokenResponse response = new ValidateTokenResponse();

        /*
         * Request дээр token байгаа эсэх болон tokens Map дотор бүртгэлтэй эсэхийг шалгаж байна.
         * Хэрэв token Map дотор байвал login хийсэн valid session гэж үзнэ.
         */
        boolean valid = request.getToken() != null && tokens.containsKey(request.getToken());

        /*
         * SOAP response дээр valid true/false утгыг тавина.
         */
        response.setValid(valid);

        /*
         * Хүн уншихад ойлгомжтой message нэмэж байна.
         */
        if (valid) {
            response.setMessage("Token valid");
        } else {
            response.setMessage("Token invalid");
        }

        return response;
    }
}