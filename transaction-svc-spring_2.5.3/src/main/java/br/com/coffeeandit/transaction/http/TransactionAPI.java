package br.com.coffeeandit.transaction.http;

import br.com.coffeeandit.transaction.business.TransactionDomain;
import br.com.coffeeandit.transaction.domain.TransactionDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@Slf4j
@RequestMapping("/v1/transaction")
@AllArgsConstructor
public class TransactionAPI {

    private TransactionDomain transactionDomain;

    @Operation(description = "API para criar uma transação financeira")
    @ResponseBody
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Retorno OK com a transação criada."),
            @ApiResponse(responseCode = "401", description = "Erro de autenticação dessa API"),
            @ApiResponse(responseCode = "403", description = "Erro de autorização dessa API"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado")})
    @PostMapping
    public Mono<TransactionDTO> criarTransacao(@Valid TransactionDTO transactionDTO) {
        return Mono.just(transactionDomain.inserirTransacao(transactionDTO));
    }

    @Operation(description = "API responsável por aprovar uma transação.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Retorno da transação Criada"

    ), @ApiResponse(responseCode = "404", description = "Dados de transação não encontrado")
            , @ApiResponse(responseCode = "400", description = "Parâmetros de requisição inválidos.")})
    @PatchMapping
    public Mono<TransactionDTO> aprovarTransacao(@Valid TransactionDTO transactionDTO) {
        return Mono.just(transactionDomain.aprovarTransacao(transactionDTO));
    }

    @Operation(description = "API responsável por rejeitar uma transação.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Retorno da transação Criada"
    ), @ApiResponse(responseCode = "404", description = "Dados de transação não encontrado")
            , @ApiResponse(responseCode = "400", description = "Parâmetros de requisição inválidos.")})
    @DeleteMapping
    public Mono<TransactionDTO> rejeitarTransacao(@Valid TransactionDTO transactionDTO) {
        return Mono.just(transactionDomain.rejeitarTransacao(transactionDTO));
    }
}
