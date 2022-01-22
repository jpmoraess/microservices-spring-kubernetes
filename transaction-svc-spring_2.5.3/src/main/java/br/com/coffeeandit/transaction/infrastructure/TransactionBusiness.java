package br.com.coffeeandit.transaction.infrastructure;

import br.com.coffeeandit.transaction.config.DomainBusinessException;
import br.com.coffeeandit.transaction.domain.SituacaoEnum;
import br.com.coffeeandit.transaction.domain.TransactionDTO;
import br.com.coffeeandit.transaction.repository.DynamoRepository;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class TransactionBusiness {


    private DynamoRepository dynamoRepository;
    public static final int AMOUNT_TO_ADD = -1;


    public TransactionBusiness(final DynamoRepository dynamoRepository) {
        this.dynamoRepository = dynamoRepository;
    }

    @NewSpan
    public Item putItem(@Valid @SpanTag(key = "transaction") final TransactionDTO transaction) {
        if (Objects.isNull(transaction) || Objects.isNull(transaction.getUui())) {
            throw new IllegalArgumentException("Argumentos ilegais para aprovar uma transação.");
        }
        if (retrieveItem(transaction.getUui().toString()).isPresent()) {
            throw new DomainBusinessException(String.format("Transaçao %s já existe", transaction.getUui()));
        }
        return dynamoRepository.putItem(transaction);

    }

    public Optional<TransactionDTO> retrieveItem(@NonNull final String uui) {

        return dynamoRepository.retrieveItem(uui);
    }

    public Optional<TransactionDTO> insertOrUpdate(@NonNull @Valid final TransactionDTO transaction) {


        var transactionDTO = retrieveItem(transaction.getUui().toString());
        if (transactionDTO.isEmpty()) {
            putItem(transaction);
            return retrieveItem(transaction.getUui().toString());
        } else {
            return updateAndRetrieveItem(transaction);
        }


    }

    @NewSpan
    public Optional<DeleteItemOutcome> removeItem(@Valid @SpanTag(key = "removeTransaction") final TransactionDTO transactionDTO) {
        Optional<TransactionDTO> retrieveItem = retrieveItem(transactionDTO.getUui().toString());
        if (retrieveItem.isPresent() && SituacaoEnum.ANALISADA.equals(retrieveItem.get().getSituacao())) {
            throw new DomainBusinessException("Transação já foi aprovada e não pode ser excluida");

        }
        return dynamoRepository.removeItem(transactionDTO);

    }

    @NewSpan
    public void aprovarTransacao(@Valid final TransactionDTO transactionDTO) {
        if (Objects.isNull(transactionDTO) || Objects.isNull(transactionDTO.getUui())) {
            throw new IllegalArgumentException("Argumentos ilegais para aprovar uma transação.");
        }
        log.info("Aprovando a transação conteudo anterior {}", transactionDTO);
        transactionDTO.analisada();
        var transaction =
                insertOrUpdate(transactionDTO);
        if (transaction.isPresent()) {
            log.info("Aprovando a transação conteudo atual {}", transaction.get());
        }


    }

    @NewSpan
    public Optional<UpdateItemOutcome> updateItem(@Valid @SpanTag(key = "updateTransaction") final TransactionDTO transactionDTO) {

        return dynamoRepository.updateItem(transactionDTO);

    }

    @NewSpan
    public Optional<TransactionDTO> updateAndRetrieveItem(@Valid @SpanTag(key = "updateTransaction") final TransactionDTO transactionDTO) {

        dynamoRepository.updateItem(transactionDTO);
        return retrieveItem(transactionDTO.getUui().toString());

    }

    public List<TransactionDTO> queryTransaction(@NotNull final Long agencia, @NotNull final Long conta) {


        var now = LocalDateTime.now();
        var lastWeek = now.plus(AMOUNT_TO_ADD, ChronoUnit.WEEKS);
        return queryTransaction(agencia, conta, lastWeek, now);

    }

    public List<TransactionDTO> queryTransactionFewSeconds(final Long agencia, final Long conta) {


        var now = LocalDateTime.now();
        var lastWeek = now.plus(-10, ChronoUnit.SECONDS);
        return queryTransaction(agencia, conta, lastWeek, now);

    }


    public List<TransactionDTO> queryTransaction(final Long agencia, final Long conta,
                                                 final LocalDateTime start, final LocalDateTime end
    ) {

        return dynamoRepository.queryTransaction(agencia, conta, start, end);

    }


}
