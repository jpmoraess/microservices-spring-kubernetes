package br.com.coffeeandit.transaction;

import br.com.coffeeandit.transaction.events.observer.SlackTransactionObserver;
import br.com.coffeeandit.transaction.events.observer.TransactionObserverService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@SpringBootApplication
@ComponentScan("br.com.coffeeandit")
@EnableKafka
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class TransactionServiceApplication {

    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final Locale LOCALE = new Locale("pt",
            "br");

    public static void main(String[] args) {
        System.setProperty("spring.kafka.consumer.client-id", "transacao" + System.currentTimeMillis());
        SpringApplication.run(TransactionServiceApplication.class, args);
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

    @Bean
    public TransactionObserverService transactionObserverService(SlackTransactionObserver slackTransactionObserver) {

        var transactionObserverService = new TransactionObserverService();
        transactionObserverService.addObserver(slackTransactionObserver);
        return transactionObserverService;
    }
}
