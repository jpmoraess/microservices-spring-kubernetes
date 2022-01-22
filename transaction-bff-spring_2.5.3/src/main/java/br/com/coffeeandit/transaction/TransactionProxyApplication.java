package br.com.coffeeandit.transaction;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.web.reactive.config.EnableWebFlux;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


@SpringBootApplication
@EnableWebFlux
@EnableWebFluxSecurity
@ComponentScan("br.com.coffeeandit")
@SecurityScheme(name = "bearerAuth", scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER, bearerFormat = "JWT")
@EnableCaching
public class TransactionProxyApplication {

    public static final Locale LOCALE = new Locale("pt",
            "br");

    public static void main(String[] args) throws Exception {
        SpringApplication.run(TransactionProxyApplication.class);

    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        var mapper = new ObjectMapper();
        var timeModule = new JavaTimeModule();
        mapper.registerModule(timeModule);
        timeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(
                        DateTimeFormatter.
                                ofPattern("yyyy-MM-dd HH:mm:ss",
                                        LOCALE)));
        return mapper;
    }

}
