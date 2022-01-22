package br.com.coffeeandit.transaction.business;

import br.com.coffeeandit.transaction.config.DomainBusinessException;
import br.com.coffeeandit.transaction.domain.BeneficiatioDto;
import br.com.coffeeandit.transaction.domain.Conta;
import br.com.coffeeandit.transaction.domain.SituacaoEnum;
import br.com.coffeeandit.transaction.domain.TransactionDTO;
import br.com.coffeeandit.transaction.infrastructure.TransactionBusiness;
import br.com.coffeeandit.transaction.repository.DynamoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ContextConfiguration(classes = {TransactionBusiness.class})
@TestPropertySource(properties = {"app.topic=transaction"})
public class TransactionBusinessTest {

    @MockBean
    private DynamoRepository dynamoRepository;


    @Autowired
    private TransactionBusiness transactionBusiness;

    private UUID uuid;

    @BeforeEach
    public void setUp() {
        uuid = UUID.randomUUID();
    }

    @Test
    public void testUpdate() {
        var dataMock = getDataMock();
        Mockito.when(transactionBusiness.retrieveItem(uuid.toString())).thenReturn(Optional.of(dataMock));
        var transactionDTO = transactionBusiness.insertOrUpdate(dataMock);
        assertThat(transactionDTO.get().getUui(), is(notNullValue()));
    }

    @Test
    public void testAprovarTransacao() {
        var dataMock = getDataMock();
        Mockito.when(transactionBusiness.retrieveItem(uuid.toString())).thenReturn(Optional.of(dataMock));
        transactionBusiness.aprovarTransacao(dataMock);
        assertThat(dataMock.getSituacao(), is(SituacaoEnum.ANALISADA));
    }

    @Test
    public void testAprovarTransacaoSemId() {
        var dataMock = getDataMock();
        dataMock.setUui(null);
        assertThrows(IllegalArgumentException.class, () -> {
            transactionBusiness.aprovarTransacao(dataMock);
        });
    }

    @Test
    public void testDelete() {
        var dataMock = getDataMock();
        dataMock.analisada();
        Mockito.when(transactionBusiness.retrieveItem(uuid.toString())).thenReturn(Optional.of(dataMock));
        assertThrows(DomainBusinessException.class, () -> {
            transactionBusiness.removeItem(dataMock);
        });
    }

    @Test
    public void tesQueryTransaction() {
        var dataMock = getDataMock();
        dataMock.analisada();
        var list = List.of(dataMock);
        Mockito.when(dynamoRepository.queryTransaction(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(list);
        List<TransactionDTO> transactionDTOS = transactionBusiness.queryTransaction(1l, 1l);
        assertThat(transactionDTOS, is(hasItem(dataMock)));
    }

    @Test
    public void testPutItemSemId() {
        var dataMock = getDataMock();
        Mockito.when(transactionBusiness.retrieveItem(uuid.toString())).thenReturn(Optional.of(getDataMock()));
        dataMock.setUui(null);
        assertThrows(IllegalArgumentException.class, () -> {
            transactionBusiness.putItem(dataMock);
        });
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
