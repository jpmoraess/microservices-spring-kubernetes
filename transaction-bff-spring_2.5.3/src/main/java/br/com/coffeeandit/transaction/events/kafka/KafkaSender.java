package br.com.coffeeandit.transaction.events.kafka;

import br.com.coffeeandit.transaction.events.dto.TransactionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@Transactional
@Slf4j
public class KafkaSender {

    public static final String
            HEADER_VALUE = "999";


    @Value("${app.topic}")
    private String topic;

    public KafkaSender(ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    private ObjectMapper objectMapper;
    private KafkaTemplate<String, String> kafkaTemplate;

    public TransactionDTO send(final TransactionDTO transactionDTO) {


        transactionDTO.setUui(getId());


        try {

            String payload = objectMapper.writeValueAsString(transactionDTO);
            Message<String> message = MessageBuilder
                    .withPayload(payload)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader(KafkaHeaders.MESSAGE_KEY, HEADER_VALUE)
                    .setHeader(KafkaHeaders.PARTITION_ID, 0)
                    .setHeader("X-CoffeeAndIT-Header", "transaction" + transactionDTO.getUui())
                    .build();

            kafkaTemplate.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        return transactionDTO;
    }

    private UUID getId() {
        return UUID.randomUUID();
    }
}