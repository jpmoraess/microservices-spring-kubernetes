package br.com.coffeeandit.transaction.http;

import br.com.coffeeandit.transaction.infrastructure.TransactionBusiness;
import br.com.coffeeandit.transaction.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.notNullValue;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = TransactionController.class, properties = "spring.profiles.active=test")
@ContextConfiguration(classes = {TransactionBusiness.class, TransactionController.class})
public class TransactionControllerTest {

    @Autowired
    private WebTestClient webClient;

    private UUID uuid;

    @MockBean
    private TransactionBusiness transactionBusiness;

    private TransactionDTO transactionDTO;

    @BeforeEach
    public void setUp() {
        uuid = UUID.randomUUID();
        transactionDTO = getDataMock();
    }

    @Test
    @DisplayName("Teste de busca de transações pelo Id.")
    @Tag("V1")
    public void testFindById() {
        String uui = uuid.toString();
        Mockito.when(transactionBusiness.retrieveItem(uui)).thenReturn(Optional.of(transactionDTO));
        webClient.
                get()
                .uri("/v1/transactions/" + uui)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header("Content-type", MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isOk().expectBody().jsonPath("$.conta", notNullValue()).hasJsonPath()
                .jsonPath("$.beneficiario", notNullValue());
    }

    @Test
    @DisplayName("Teste de busca de transações pelo Id.")
    @Tag("V1")
    public void testNotFoundById() {
        String uui = uuid.toString();
        Mockito.when(transactionBusiness.retrieveItem(uui)).thenReturn(Optional.empty());
        webClient.
                get()
                .uri("/v1/transactions/" + uui)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header("Content-type", MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isNotFound();
    }


    @Test
    @DisplayName("Teste de deleção de transações pelo Id.")
    @Tag("V1")
    public void testDeleteById() {
        String uui = uuid.toString();
        Mockito.when(transactionBusiness.retrieveItem(uui)).thenReturn(Optional.of(transactionDTO));
        webClient.
                delete().uri("/v1/transactions/" + uui)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header("Content-type", MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("Teste de Alteração de transações pelo Id.")
    @Tag("V1")
    public void testPatchById() {
        String uui = uuid.toString();
        var alteracaoSituacaoDTO = new AlteracaoSituacaoDTO();
        alteracaoSituacaoDTO.setSituacao(SituacaoEnum.REJEITADA);
        Mockito.when(transactionBusiness.retrieveItem(uui)).thenReturn(Optional.of(transactionDTO));
        ///Mockito.when(transactionBusiness.updateItem(transactionDTO)).thenReturn(Optional.of(new UpdateItemOutcome(new UpdateItemResult())));
        Mockito.when(transactionBusiness.updateAndRetrieveItem(transactionDTO)).thenReturn(Optional.of(transactionDTO));
        webClient.
                patch().uri("/v1/transactions/" + uui)
                .body(BodyInserters.fromValue(alteracaoSituacaoDTO))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header("Content-type", MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isNoContent();
    }

    private TransactionDTO getDataMock() {


        var transactionDTO = new TransactionDTO();
        transactionDTO.setData(LocalDateTime.now());
        Conta conta = new Conta();
        conta.setCodigoAgencia(1210l);
        conta.setCodigoConta(1242l);
        transactionDTO.setConta(conta);
        transactionDTO.setUui(uuid);
        transactionDTO.naoAnalisada();
        var beneficiario = new BeneficiatioDto();
        beneficiario.setAgencia("1219");
        beneficiario.setCodigoBanco(1212l);
        beneficiario.setCPF(12240181l);
        beneficiario.setNomeFavorecido("Favorecido");
        transactionDTO.setBeneficiario(beneficiario);

        return transactionDTO;

    }

}
