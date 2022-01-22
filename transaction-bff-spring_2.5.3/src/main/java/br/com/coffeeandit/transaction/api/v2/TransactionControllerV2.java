package br.com.coffeeandit.transaction.api.v2;

import br.com.coffeeandit.transaction.events.dto.RequisicaoTransacaoDTO;
import br.com.coffeeandit.transaction.events.dto.SituacaoEnum;
import br.com.coffeeandit.transaction.events.dto.TransactionDTO;
import br.com.coffeeandit.transaction.events.kafka.KafkaSender;
import br.com.coffeeandit.transaction.http.TransactionHttpService;
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
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.FileNotFoundException;
import java.time.Duration;

@RestController
@Slf4j
@RequestMapping("/v2")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "/v2/transactions", description = "Grupo de API's para manipulação de transações financeiras")
public class TransactionControllerV2 {

    @Value("${app.timeout}")
    private int timeout;
    @Value("${app.retries}")
    public int numberRetries;
    private KafkaSender kafkaSender;
    private TransactionHttpService transactionHttpService;

    public TransactionControllerV2(final KafkaSender kafkaSender, final TransactionHttpService transactionHttpService) throws FileNotFoundException {
        this.kafkaSender = kafkaSender;
        this.transactionHttpService = transactionHttpService;


    }

    @Operation(description = "API para criar uma transação financeira", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(path = "/transactions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Retorno OK com a transação criada."),
            @ApiResponse(responseCode = "401", description = "Erro de autenticação dessa API"),
            @ApiResponse(responseCode = "403", description = "Erro de autorização dessa API"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado")})
    public Mono<TransactionDTO> save( @Valid @RequestBody RequisicaoTransacaoDTO transactionDTO) {

        return Mono.just(
                        kafkaSender.send(transactionDTO)

                ).timeout(Duration.ofSeconds(timeout))
                .doOnSuccess(result -> {
                    log.info(String.format("Property sended.: %s", result.toString()));
                })
                .doOnError(throwable -> log.error(throwable.getLocalizedMessage()))
                .doFirst(() -> changeStatusUnanalyzed(transactionDTO))
                .retry(numberRetries);
    }

    @Operation(description = "API para alterar a situação de transação financeira", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping(path = "/transactions/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Parameters({@Parameter(in = ParameterIn.PATH, description = "Hash da Transação", name = "id"),
            @Parameter(in = ParameterIn.QUERY, description = "Status da Transação", name = "situacao")
    })

    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Retorno para alteração de uma transação"),
            @ApiResponse(responseCode = "401", description = "Erro de autenticação dessa API"),
            @ApiResponse(responseCode = "403", description = "Erro de autorização dessa API"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado")})
    public ResponseEntity patch( @PathVariable("id") String uuid, @RequestParam SituacaoEnum situacao,
                                @RequestHeader(name = "content-type", defaultValue = MediaType.APPLICATION_JSON_VALUE) String contentType

    ) {
        transactionHttpService.alterarSituacao(uuid, situacao);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    @Operation(description = "API para remover as transações persistidas", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping(value = "/transactions/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteById( @PathVariable("id") String uuid, @RequestHeader(name = "content-type", defaultValue = MediaType.APPLICATION_JSON_VALUE) String contentType) {

        transactionHttpService.removeById(uuid);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private void changeStatusUnanalyzed(TransactionDTO transactionDTO) {
        transactionDTO.naoAnalisada();
    }

    @Operation(description = "API para buscar os transações persistidas por agência e conta", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Retorno OK da Lista de transações"),
            @ApiResponse(responseCode = "401", description = "Erro de autenticação dessa API"),
            @ApiResponse(responseCode = "403", description = "Erro de autorização dessa API"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado")})
    @Parameters({@Parameter(in = ParameterIn.QUERY, description = "Número da Conta", name = "conta"),
            @Parameter(in = ParameterIn.QUERY, description = "Número da Agência", name = "agencia")})
    @GetMapping(value = "/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<TransactionDTO> queryTransaction(@RequestParam("conta") final Long conta, @RequestParam("agencia") final Long agencia
    ) {
        return transactionHttpService.queryTransactionBlock(conta, agencia);


    }

    @Operation(description = "API para buscar  transações pelo Id", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Retorno OK com a transação encontrada."),
            @ApiResponse(responseCode = "401", description = "Erro de autenticação dessa API"),
            @ApiResponse(responseCode = "403", description = "Erro de autorização dessa API"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado")})

    @Parameters(@Parameter(in = ParameterIn.PATH, description = "Id da transação", name = "id"))
    @GetMapping(value = "/transactions/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TransactionDTO> findById( @PathVariable("id") String uuid, @RequestHeader(name = "content-type", defaultValue = MediaType.APPLICATION_JSON_VALUE) String contentType) {
        return Mono.just(transactionHttpService.findById(uuid));
    }
}