package br.com.coffeeandit.transaction.repository;

import br.com.coffeeandit.transaction.domain.*;
import br.com.coffeeandit.transaction.dynamob.DynamoTable;
import br.com.coffeeandit.transaction.dynamob.InsertableItem;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

@Component
@Slf4j
public class DynamoRepository implements DynamoTable, InsertableItem<TransactionDTO> {


    public static final String TRANSACAO = "transacao";
    public static final String UUI = "uui";
    public static final String VALOR = "valor";
    public static final String TIPOTRANSACAO = "tipotransacao";
    public static final String CONTA = "conta";
    public static final String AGENCIA = "agencia";
    public static final String AGENCIA_BENEFICIARIO = "agenciaBeneficiario";
    public static final String CONTA_BENEFICIARIO = "contaBeneficiario";
    public static final String CPF = "cpf";
    public static final String NOME_FAVORECIDO = "nomeFavorecido";
    public static final String BANCO_FAVORECIDO = "bancoFavorecido";

    public static final String DT_TRANSACTION = "dtTransaction";
    public static final String TRANSACOES_SEMANA = "agencia = :agencia AND  conta = :conta AND dtTransaction BETWEEN :dataInicial and :dataFinal";
    public static final int AMOUNT_TO_ADD = -1;
    public static final String STATUS_RISCO = "situacao";

    private Table tableTransacao;
    private DynamoDB dynamoDB;
    private AmazonDynamoDB amazonDynamoDB;

    private Function<Item, TransactionDTO> itemTransactionDTOFunction = item -> {
        var transacaoDto = new TransactionDTO();
        transacaoDto.setValor(item.getNumber(VALOR));
        String dtTransaction = item.getString(DT_TRANSACTION);
        transacaoDto.setData(LocalDateTime.now());
        if (Objects.nonNull(dtTransaction)) {
            transacaoDto.setData(LocalDateTime.parse(dtTransaction));
        }
        transacaoDto.setTipoTransacao(TipoTransacao.valueOf(item.getString(TIPOTRANSACAO)));
        transacaoDto.setUui(UUID.fromString(item.getString(UUI)));
        var conta = new Conta();
        conta.setCodigoConta(item.getLong(CONTA));
        conta.setCodigoAgencia(item.getLong(AGENCIA));
        transacaoDto.setConta(conta);
        transacaoDto.setSituacao(SituacaoEnum.valueOf(item.getString(STATUS_RISCO)));
        var beneficiario = new BeneficiatioDto();
        beneficiario.setAgencia(item.getString(AGENCIA_BENEFICIARIO));
        beneficiario.setCodigoBanco(item.getLong(BANCO_FAVORECIDO));
        beneficiario.setConta(item.getString(CONTA_BENEFICIARIO));
        beneficiario.setCPF(item.getLong(CPF));
        beneficiario.setNomeFavorecido(item.getString(NOME_FAVORECIDO));
        transacaoDto.setBeneficiario(beneficiario);


        return transacaoDto;
    };


    public DynamoRepository(final DynamoDB dynamoDB, final AmazonDynamoDB amazonDynamoDB) {
        this.dynamoDB = dynamoDB;
        this.amazonDynamoDB = amazonDynamoDB;
    }

    @Override
    @NewSpan
    public Item putItem(@SpanTag(key = "transaction") final TransactionDTO transaction) {

        log.info(String.format("Inserindo a transação %s", transaction));
        var table = getTable();
        var conta = transaction.getConta();
        var beneficiario = transaction.getBeneficiario();
        var item = getItem(transaction, conta, beneficiario);
        var putItemOutcome = table.putItem(item);
        var putItemOutcomeItem = putItemOutcome.getItem();
        return Objects.nonNull(putItemOutcome.getItem()) ? putItemOutcomeItem : item;

    }

    public Optional<TransactionDTO> retrieveItem(final String uui) {
        var item = getTable().getItem(new PrimaryKey(UUI, uui));
        if (Objects.nonNull(item)) {
            return Optional.ofNullable(itemTransactionDTOFunction.apply(item));
        }

        return Optional.empty();

    }


    @NewSpan
    public Optional<DeleteItemOutcome> removeItem(@SpanTag(key = "removeTransaction") final TransactionDTO transactionDTO) {
        var table = getTable();


        DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
                .withPrimaryKey(new PrimaryKey(UUI, transactionDTO.getUui().toString()));

        return Optional.of(table.deleteItem(deleteItemSpec));


    }

    @NewSpan
    public Optional<UpdateItemOutcome> updateItem(@SpanTag(key = "updateTransaction") final TransactionDTO transactionDTO) {
        var table = getTable();

        var updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey(new PrimaryKey(UUI, transactionDTO.getUui().toString()))
                .withUpdateExpression("set situacao = :s")
                .withValueMap(new ValueMap().withString(":s", transactionDTO.getSituacao().toString()))
                .withReturnValues(ReturnValue.UPDATED_NEW);


        var updateItemOutcome = Optional.of(table.updateItem(updateItemSpec));
        log.info("UpdateItem succeeded {}{}", updateItemOutcome.get().getItem().toJSONPretty(), transactionDTO.getUui().toString());
        return updateItemOutcome;


    }


    public List<TransactionDTO> queryTransaction(final Long agencia, final Long conta,
                                                 final LocalDateTime start, final LocalDateTime end
    ) {


        var expressionAttributeValues = new HashMap<String, Object>();
        expressionAttributeValues.put(":".concat(AGENCIA), agencia);
        expressionAttributeValues.put(":".concat(CONTA), conta);
        expressionAttributeValues.put(":dataInicial", start.toString());
        expressionAttributeValues.put(":dataFinal", end.toString());


        return mapToTransactionDTO(getTable().scan(TRANSACOES_SEMANA,
                null,
                expressionAttributeValues));

    }

    protected List<TransactionDTO> mapToTransactionDTO(final ItemCollection<ScanOutcome> scanOutcome) {

        var transactions = new ArrayList<TransactionDTO>();

        scanOutcome.forEach(item -> {
            transactions.add(itemTransactionDTOFunction.apply(item));
        });
        transactions.sort(Comparator.comparing(TransactionDTO::getData));
        return transactions;
    }

    private Item getItem(TransactionDTO transaction, Conta conta, BeneficiatioDto beneficiario) {
        if (Objects.isNull(transaction.getData())) {
            transaction.setData(LocalDateTime.now());
        }


        var item = new HashMap<String, AttributeValue>();
        item.put(UUI, new AttributeValue().withS(transaction.getUui().toString()));
        item.put(VALOR, new AttributeValue().withN(transaction.getValor().toPlainString()));

            return new Item().withPrimaryKey(UUI, transaction.getUui().toString())
                .withNumber(VALOR, transaction.getValor().doubleValue())
                .withString(TIPOTRANSACAO, transaction.getTipoTransacao().name())
                .withLong(CONTA, conta.getCodigoConta())
                .withLong(AGENCIA, conta.getCodigoAgencia())
                .withString(AGENCIA_BENEFICIARIO, beneficiario.getAgencia())
                .withString(CONTA_BENEFICIARIO, beneficiario.getConta())
                .withLong(CPF, beneficiario.getCPF())
                .withString(NOME_FAVORECIDO, beneficiario.getNomeFavorecido())
                .withLong(BANCO_FAVORECIDO, beneficiario.getCodigoBanco())
                .withString(DT_TRANSACTION, transaction.getData().toString())
                .with(STATUS_RISCO, transaction.getSituacao().toString());

    }

    public Table getTable() {
        if (Objects.isNull(tableTransacao)) {
            tableTransacao = dynamoDB.getTable(TRANSACAO);
        }
        return tableTransacao;
    }
}
