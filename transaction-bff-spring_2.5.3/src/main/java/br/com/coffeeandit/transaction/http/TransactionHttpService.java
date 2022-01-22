package br.com.coffeeandit.transaction.http;

import br.com.coffeeandit.transaction.events.AlteracaoSituacaoDTO;
import br.com.coffeeandit.transaction.events.dto.SituacaoEnum;
import br.com.coffeeandit.transaction.events.dto.TransactionDTO;
import br.com.coffeeandit.transaction.exception.InfrastructureException;
import br.com.coffeeandit.transaction.exception.NotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Service
@Log4j2
public class TransactionHttpService {

    public static final String ACCEPT = "accept";
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    private ObjectMapper objectMapper;

    @Value("${app.urlTransaction}")
    private String queryTransaction;

    @Value("${app.urlTransactionById}")
    private String urlTransactionById;


    public TransactionHttpService(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Flux<TransactionDTO> queryTransactionBlock(
            final Long conta, final Long agencia
    ) {
        return Flux.fromIterable(queryTransaction(agencia, conta))
                .limitRate(100).cache(Duration.ofSeconds(3));
    }

    @Cacheable(value = "transactions", key = "#uuid")
    public TransactionDTO findById(String uuid) {
        var urlTransaction = String.format(urlTransactionById, uuid);
        log.info("Buscando uuid - {} de {}", uuid, urlTransaction);

        var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(
                        urlTransaction
                ))
                .setHeader(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .setHeader("Content-type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("response status code {}", response.statusCode());
            if (response.statusCode() == HttpStatus.OK.value()) {
                return objectMapper.readValue(response.body(), TransactionDTO.class);

            } else if (response.statusCode() == HttpStatus.NOT_FOUND.value()) {
                throw new NotFoundException(String.format("Não foi possivel encontrar a transação %s", uuid));
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new InfrastructureException(e);
        }
        return null;
    }

    public void removeById(final String uuid) {
        var urlTransaction = String.format(urlTransactionById, uuid);

        var request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(
                        urlTransaction
                ))
                .setHeader(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .setHeader("Content-type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == HttpStatus.NO_CONTENT.value()) {
                return;
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new InfrastructureException(e);

        }
        throw new NotFoundException(String.format("Não foi possivel remover a transação %s", uuid));

    }

    public void alterarSituacao(final String uuid, final AlteracaoSituacaoDTO alteracaoSituacaoDTO) {
        try {
            var urlTransaction = String.format(urlTransactionById, uuid);
            log.info("Alterando a situação uuid - {} de {}", uuid, urlTransaction);

            var request = HttpRequest.newBuilder()
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(alteracaoSituacaoDTO)))
                    .uri(URI.create(
                            urlTransaction
                    ))
                    .setHeader(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .setHeader("Content-type", MediaType.APPLICATION_JSON_VALUE)
                    .build();
            try {
                var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == HttpStatus.NO_CONTENT.value()) {
                    return;
                }
            } catch (IOException | InterruptedException e) {
                log.error(e.getMessage(), e);
            }
            throw new NotFoundException(String.format("Não foi possivel atualizar a situacao da transação %s", uuid));
        } catch (JsonProcessingException e) {
            throw new InfrastructureException(e);
        }

    }

    public void alterarSituacao(final String uuid, final SituacaoEnum situacaoEnum) {

        var alteracaoSituacaoDTO = new AlteracaoSituacaoDTO();
        alteracaoSituacaoDTO.setSituacao(situacaoEnum);
        alterarSituacao(uuid, alteracaoSituacaoDTO);

    }

    private List<TransactionDTO> queryTransaction(@NotNull final Long agencia, @NotNull final Long conta) {

        var urlTransaction = String.format(queryTransaction, agencia, conta);

        var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(
                        urlTransaction
                ))
                .setHeader(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == HttpStatus.OK.value()) {
                var transactionDTOS = objectMapper.readValue(response.body(), new TypeReference<List<TransactionDTO>>() {
                });
                if (transactionDTOS.isEmpty()) {
                    throw new NotFoundException(String.format("Não foi possivel encontrar dados para agência %s e conta %s", agencia, conta));
                }
                return transactionDTOS;

            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }
}
