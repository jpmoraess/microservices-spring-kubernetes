package br.com.coffeeandit.limites.business;

import br.com.coffeeandit.limites.dto.TransactionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@Slf4j
public class KafkaSender {


    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.returnTopic}")
    private String topic;

    private ObjectMapper objectMapper;

    public KafkaSender(final KafkaTemplate kafkaTemplate, final ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void send(final TransactionDTO transactionDTO) {
        try {

            String payload = objectMapper.writeValueAsString(transactionDTO);
            Message<String> message = MessageBuilder
                    .withPayload(payload)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader(KafkaHeaders.PARTITION_ID, 0)
                    .build();

            kafkaTemplate.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}