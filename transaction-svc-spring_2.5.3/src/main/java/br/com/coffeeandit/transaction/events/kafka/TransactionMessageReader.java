package br.com.coffeeandit.transaction.events.kafka;


import br.com.coffeeandit.transaction.business.TransactionDomain;
import br.com.coffeeandit.transaction.domain.SituacaoEnum;
import br.com.coffeeandit.transaction.domain.TransactionDTO;
import br.com.coffeeandit.transaction.events.observer.TransactionObserverService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.annotation.ContinueSpan;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class TransactionMessageReader {


    public static final String LIQUIDACAO = "liquidacao";

    public TransactionMessageReader(ObjectMapper objectMapper, TransactionDomain transactionDomain, TransactionObserverService transactionObserverService, KafkaTemplate<String, String> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.transactionDomain = transactionDomain;
        this.transactionObserverService = transactionObserverService;
        this.kafkaTemplate = kafkaTemplate;
    }

    private final ObjectMapper objectMapper;
    private final TransactionDomain transactionDomain;
    private final TransactionObserverService transactionObserverService;
    private final KafkaTemplate<String, String> kafkaTemplate;


    @KafkaListener(topics = "${app.topic}", clientIdPrefix = "subscriberTransaction")
    public void onConsume(final String message, final Acknowledgment acknowledgment) {

        log.info("Mensagem chega para leitura.: " + message);

        CompletableFuture.supplyAsync(() -> {
            try {
                var transaction = getTransaction(message);
                transactionDomain.inserirTransacao(transaction);
                acknowledgment.acknowledge();
                return transaction;
            } catch (IOException exception) {
                log.error(exception.getMessage(), exception);
                throw new RuntimeException(exception);
            }
        }).whenCompleteAsync((item, throwable) -> {
            if (Objects.nonNull(throwable)) {
                log.error(throwable.getMessage(), throwable);
            }

        });


    }

    @KafkaListener(topics = "${app.returnTopic}", clientIdPrefix = "subscriberLimites")
    public void onConsumeExtorno(final String message, final Acknowledgment acknowledgment) {
        try {
            var transaction = getTransaction(message);
            log.info("Transação retornada da Análise {}", transaction);

            if (!SituacaoEnum.ANALISADA.equals(transaction.getSituacao())) {
                log.info("Solicitação de alteração de status da transação {}", transaction);

                if (SituacaoEnum.EM_ANALISE_HUMANA.equals(transaction.getSituacao())) {
                    notification(transaction);
                }
                acknowledgment.acknowledge();
            } else {
                log.info("Transação Analisada {} ", transaction);
                transactionDomain.aprovarTransacao(transaction);
                enviarLiquidacao(transaction);
                acknowledgment.acknowledge();
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void enviarLiquidacao(TransactionDTO transaction) throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(transaction);
        Message<String> kafkaMessage = MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, LIQUIDACAO)
                .setHeader(KafkaHeaders.PARTITION_ID, 0)
                .build();

        kafkaTemplate.send(kafkaMessage);
    }

    @ContinueSpan
    public void notification(final TransactionDTO transaction) {
        transactionObserverService.notification(transaction);

    }

    private TransactionDTO getTransaction(String message) throws IOException {
        TransactionDTO transactionDTO = objectMapper.readValue(message, TransactionDTO.class);
        if (Objects.isNull(transactionDTO.getSituacao())) {
            transactionDTO.setSituacao(SituacaoEnum.NAO_ANALISADA);
        }
        transactionDTO.setData(LocalDateTime.now());
        return transactionDTO;
    }
}
