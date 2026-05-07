package com.example.authsoap.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import java.util.List;

/*
 * WebServiceConfig нь SOAP service-ийн үндсэн тохиргооны class юм.
 *
 * Энэ class дээр:
 * - SOAP Web Service-г идэвхжүүлэх
 * - /ws endpoint дээр SOAP request хүлээж авах servlet бүртгэх
 * - auth.xsd schema-аас WSDL үүсгэх
 * - Frontend/Gateway-ээс ирэх request-д CORS тохируулах
 *
 * зэрэг тохиргоонууд хийгдэнэ.
 */
@EnableWs
@Configuration
public class WebServiceConfig {

    /*
     * MessageDispatcherServlet нь Spring Web Services-ийн SOAP request хүлээж авдаг servlet юм.
     *
     * Энгийн REST controller шиг биш, SOAP service нь XML body-тэй request авдаг.
     * Тиймээс Spring WS-ийн MessageDispatcherServlet ашиглаж SOAP message-ийг endpoint method руу дамжуулна.
     *
     * Энэ servlet-ийг "/ws/*" path дээр бүртгэж байгаа.
     * Жишээ нь:
     * http://localhost:8081/ws
     *
     * Gateway дээрээс SOAP request ирэхэд энэ servlet эхэлж хүлээж авна.
     */
    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
            ApplicationContext applicationContext) {

        /*
         * SOAP message dispatch хийх servlet үүсгэж байна.
         */
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();

        /*
         * Spring application context-ийг servlet-д өгч байна.
         * Ингэснээр servlet нь @Endpoint class-уудыг олж ашиглаж чадна.
         */
        servlet.setApplicationContext(applicationContext);

        /*
         * WSDL доторх service location URL-ийг runtime орчинд зөв болгож өөрчлөхөд ашиглагдана.
         * Cloud дээр deploy хийх үед WSDL location зөв харагдахад хэрэгтэй.
         */
        servlet.setTransformWsdlLocations(true);

        /*
         * Servlet-ийг "/ws/*" endpoint дээр бүртгэнэ.
         * Энэ нь SOAP service-ийн public path болж ажиллана.
         */
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    /*
     * WSDL definition үүсгэж байна.
     *
     * SOAP service хэрэглэгчид ямар operation, request, response бүтэцтэйг WSDL-ээр мэддэг.
     * Энэ bean нь auth.xsd schema дээр үндэслэн WSDL үүсгэнэ.
     *
     * Bean-ийн нэр "auth" тул WSDL-ийг дараах байдлаар харах боломжтой:
     * /ws/auth.wsdl
     */
    @Bean(name = "auth")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema authSchema) {

        /*
         * DefaultWsdl11Definition нь XSD schema-аас WSDL үүсгэхэд ашиглагдана.
         */
        DefaultWsdl11Definition wsdl = new DefaultWsdl11Definition();

        /*
         * SOAP port type нэр.
         * Энэ нь WSDL дотор service-ийн operation group нэр шиг харагдана.
         */
        wsdl.setPortTypeName("AuthPort");

        /*
         * SOAP service-ийн үндсэн endpoint URI.
         */
        wsdl.setLocationUri("/ws");

        /*
         * Namespace нь XSD болон Endpoint дээр ашиглаж байгаа namespace-тэй ижил байх ёстой.
         */
        wsdl.setTargetNamespace("http://example.com/authsoap");

        /*
         * auth.xsd schema-г WSDL definition-д холбож байна.
         */
        wsdl.setSchema(authSchema);

        return wsdl;
    }

    /*
     * SOAP request/response-ийн бүтэц тодорхойлсон XSD schema-г уншиж байна.
     *
     * auth.xsd файл нь src/main/resources/schema/auth.xsd дотор байрладаг.
     * Энэ schema дээр RegisterUserRequest, LoginUserRequest, ValidateTokenRequest
     * болон тэдгээрийн response structure тодорхойлогдсон.
     */
    @Bean
    public XsdSchema authSchema() {
        return new SimpleXsdSchema(new ClassPathResource("schema/auth.xsd"));
    }

    /*
     * CORS filter.
     *
     * Frontend нь тусдаа domain дээр deploy хийгдсэн тул browser шууд SOAP endpoint руу
     * request илгээх үед CORS restriction гарч болно.
     *
     * Энэ filter нь зөвшөөрөгдсөн frontend origin-уудаас ирэх request-ийг зөвшөөрнө.
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {

        /*
         * CORS тохиргооны object үүсгэж байна.
         */
        CorsConfiguration config = new CorsConfiguration();

        /*
         * Request илгээхийг зөвшөөрөх origin-ууд.
         *
         * 127.0.0.1:5500 нь local development үед frontend ажиллуулахад ашиглагдана.
         * frontend-app-a4t6q.ondigitalocean.app нь DigitalOcean дээр deploy хийсэн frontend.
         */
        config.setAllowedOriginPatterns(List.of(
                "http://127.0.0.1:5500",
                "https://frontend-app-a4t6q.ondigitalocean.app"
        ));

        /*
         * SOAP service дээр зөвшөөрөх HTTP methods.
         *
         * SOAP request голчлон POST method ашигладаг.
         * Гэхдээ OPTIONS нь browser-ийн preflight request-д хэрэгтэй.
         */
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        /*
         * Бүх header-ийг зөвшөөрч байна.
         * Content-Type зэрэг SOAP XML request-д хэрэгтэй header-үүд энд багтана.
         */
        config.setAllowedHeaders(List.of("*"));

        /*
         * Cookie/session credential ашиглахгүй байгаа тул false болгосон.
         * Манай систем token-ийг Authorization header/localStorage ашиглаж дамжуулж байгаа.
         */
        config.setAllowCredentials(false);

        /*
         * CORS тохиргоог "/ws/**" path дээр бүртгэнэ.
         * Өөрөөр хэлбэл зөвхөн SOAP endpoint-д энэ CORS тохиргоо үйлчилнэ.
         */
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/ws/**", config);

        /*
         * CORS filter-ийг Spring Boot-д бүртгэж байна.
         */
        FilterRegistrationBean<CorsFilter> bean =
                new FilterRegistrationBean<>(new CorsFilter(source));

        /*
         * Энэ filter хамгийн түрүүнд ажиллах ёстой.
         * Ингэснээр browser-ийн preflight request-үүд endpoint хүрэхээс өмнө зөв handle хийгдэнэ.
         */
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return bean;
    }
}