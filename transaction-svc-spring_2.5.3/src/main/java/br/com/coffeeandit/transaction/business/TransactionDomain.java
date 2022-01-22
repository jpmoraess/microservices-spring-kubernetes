package br.com.coffeeandit.transaction.business;

import br.com.coffeeandit.transaction.domain.TransactionDTO;
import br.com.coffeeandit.transaction.infrastructure.TransactionBusiness;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TransactionDomain {
    public static final int LIMITE_MAXIMO = 10000;
    private TransactionBusiness transactionBusiness;


    public boolean analisarTransacao(@Valid final TransactionDTO transactionDTO) {

        final Optional<TransactionDTO> optionalTransactionDTO = buscarTransacao(transactionDTO);
        if (optionalTransactionDTO.isPresent()) {
            final TransactionDTO dto = optionalTransactionDTO.get();
            if (dto.getValor().compareTo(BigDecimal.valueOf(LIMITE_MAXIMO)) < 0) {
                dto.analisada();
                criarTransacao(dto);
                return true;
            }
        }
        return false;

    }



    public TransactionDTO aprovarTransacao(@Valid final TransactionDTO transactionDTO) {

        final Optional<TransactionDTO> optionalTransactionDTO = buscarTransacao(transactionDTO);
        if (optionalTransactionDTO.isPresent()) {
            final TransactionDTO dto = optionalTransactionDTO.get();
            analisarTransacao(dto);
            if (dto.isAnalisada()) {
                dto.aprovada();
                transactionBusiness.aprovarTransacao(dto);
                return dto;
            }
        }
        throw new IllegalArgumentException("Não foi possivel atualizar a transacao");

    }

    public void atualizarTransacao(TransactionDTO dto) {
        transactionBusiness.insertOrUpdate(dto);
    }

    public TransactionDTO inserirTransacao(@Valid final TransactionDTO transactionDTO) {

        transactionDTO.setUui(UUID.randomUUID());
        transactionDTO.naoAnalisada();
        criarTransacao(transactionDTO);
        final Optional<TransactionDTO> optionalTransactionDTO = buscarTransacao(transactionDTO);
        if (optionalTransactionDTO.isPresent()) {
            return optionalTransactionDTO.get();
        }
        throw new IllegalStateException("Não foi possivel inserir a transação");
    }

    private Optional<TransactionDTO> buscarTransacao(@Valid TransactionDTO transactionDTO) {
        return transactionBusiness.retrieveItem(transactionDTO.getUui().toString());
    }

    public TransactionDTO rejeitarTransacao(@Valid final TransactionDTO transactionDTO) {

        final Optional<TransactionDTO> optionalTransactionDTO = buscarTransacao(transactionDTO);
        if (optionalTransactionDTO.isPresent()) {
            final TransactionDTO dto = optionalTransactionDTO.get();
            dto.rejeitada();
            atualizarTransacao(dto);
            return dto;
        }
        throw new IllegalArgumentException("Não foi possivel localizar a transacao");
    }
    private void criarTransacao(TransactionDTO dto) {
        transactionBusiness.insertOrUpdate(dto);
    }
}
