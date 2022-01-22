package br.com.coffeeandit.transaction.http;

import br.com.coffeeandit.transaction.config.NotFoundResponse;
import br.com.coffeeandit.transaction.domain.AlteracaoSituacaoDTO;
import br.com.coffeeandit.transaction.domain.TransactionDTO;
import br.com.coffeeandit.transaction.infrastructure.TransactionBusiness;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.Duration;
import java.util.List;

@RestController
@Slf4j
@RequestMapping(value = "/v1",
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "/v1/transactions", description = "Grupo de API's para manipulação de transações financeiras")
public class TransactionController {

    public static final String TRANSACTION_EVENT = "transaction-event";
    @Value("${app.timeout}")
    private int timeout;
    @Value("${app.cacheTime}")
    public int cacheTime;
    private TransactionBusiness transactionBusiness;
    @Value("${app.intervalTransaction}")
    private int intervalTransaction;

    public TransactionController(final TransactionBusiness transactionBusiness) {
        this.transactionBusiness = transactionBusiness;
    }

    @Operation(description = "API responsável por retornar um SSE com Flux com as transações por período agência e conta.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Retorno OK com a transação criada."),
            @ApiResponse(responseCode = "401", description = "Erro de autenticação dessa API"),
            @ApiResponse(responseCode = "403", description = "Erro de autorização dessa API"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado")})
    @GetMapping(value = "/transactions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Parameters({@Parameter(in = ParameterIn.QUERY, description = "número da conta", name = "conta", example = "07421"),
            @Parameter(in = ParameterIn.QUERY, description = "número da agẽncia", name = "agencia", example = "0209")
    })
    public Flux<ServerSentEvent<List<TransactionDTO>>> queryTransaction(
            @RequestParam("conta") final Long conta,
            @RequestParam("agencia") final Long agencia
    ) {

        return Flux.interval(Duration.ofSeconds(intervalTransaction))
                .map(sequence -> ServerSentEvent.<List<TransactionDTO>>builder()
                        .id(String.valueOf(sequence))
                        .event(TRANSACTION_EVENT)
                        .data(transactionBusiness.queryTransactionFewSeconds(agencia, conta))
                        .build())
                .doOnError(throwable -> {
                    log.error(throwable.getMessage(), throwable);
                });


    }


    @Operation(description = "API para alterar a situação de transação financeira", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping(path = "/transactions/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Retorno para alteração de uma transação"),
            @ApiResponse(responseCode = "401", description = "Erro de autenticação dessa API"),
            @ApiResponse(responseCode = "403", description = "Erro de autorização dessa API"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado")})
    @Parameters({@Parameter(in = ParameterIn.PATH, description = "Hash da Transação", name = "id"),
            @Parameter(in = ParameterIn.QUERY, description = "Status da Transação", name = "situacao")
    })
    public ResponseEntity patch(@PathVariable("id") String uuid, @Valid @RequestBody AlteracaoSituacaoDTO alteracaoSituacaoDTO) {
        var item = transactionBusiness.retrieveItem(uuid);
        if (item.isPresent()) {
            var transactionDTO = item.get();
            log.info("Transação recuperada para atualização %s ", transactionDTO);
            transactionDTO.setSituacao(alteracaoSituacaoDTO.getSituacao());
            log.info("Situação da Transação alterada %s ", transactionDTO);
            var updateItem = transactionBusiness.updateAndRetrieveItem(transactionDTO);
            if (updateItem.isPresent()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

            }
        }
        throw new NotFoundResponse(String.format("Não foi possível alterar a transação %s", uuid));

    }

    @DeleteMapping(value = "/transactions/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteById(@PathVariable("id") String uuid) {
        var item = transactionBusiness.retrieveItem(uuid);
        if (item.isPresent()) {
            transactionBusiness.removeItem(item.get());
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping(value = "/transactions/block", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<TransactionDTO> queryTransactionBlock(
            @RequestParam("conta") final Long conta, @RequestParam("agencia") final Long agencia
    ) {
        return Flux.fromIterable(transactionBusiness.queryTransaction(agencia, conta))
                .limitRate(100).cache(Duration.ofMinutes(5));


    }

    @GetMapping(value = "/transactions/version", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getFakeVersion() {
        return ResponseEntity.ok("V2");


    }

    @GetMapping(value = "/transactions/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TransactionDTO> findById(@PathVariable("id") String uuid) {
        var item = transactionBusiness.retrieveItem(uuid);
        if (item.isPresent()) {
            return Mono.just(item.get());
        }
        throw new NotFoundResponse("Transação não encontrada");
    }
}