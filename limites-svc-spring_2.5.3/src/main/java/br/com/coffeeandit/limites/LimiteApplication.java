package br.com.coffeeandit.limites;

import br.com.coffeeandit.limites.domain.LimiteDiario;
import br.com.coffeeandit.limites.domain.LimiteDiarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableCaching
@EnableKafka
@EnableJpaRepositories(basePackageClasses = LimiteDiarioRepository.class)
@EntityScan(basePackageClasses = LimiteDiario.class)
@ComponentScan("br.com.coffeeandit")
public class LimiteApplication {

    @Value("${spring.cache.expireAfterWrite}")
    public int expireAfterWrite;
    @Value("${spring.cache.maximumSize}")
    public int maximumSize;
    @Value("${spring.cache.allowNullValues}")
    public boolean allowNullValues;
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final Locale LOCALE = new Locale("pt", "br");


    public static void main(String[] args) {
        System.setProperty("spring.kafka.consumer.client-id", "limite" + System.currentTimeMillis());
        SpringApplication.run(LimiteApplication.class, args);
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        var mapper = new ObjectMapper();
        var timeModule = new JavaTimeModule();
        mapper.registerModule(timeModule);
        timeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS, LOCALE)));
        return mapper;
    }
}
