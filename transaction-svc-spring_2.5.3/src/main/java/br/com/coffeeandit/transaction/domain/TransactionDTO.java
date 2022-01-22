package br.com.coffeeandit.transaction.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(of = "uui")
@ToString
@Schema(description = "Objeto de transporte para o envio de uma promessa de transação")
public class TransactionDTO implements Serializable {

    private static final long serialVersionUID = 2806421523585360625L;

    @NotNull
    @Schema(description = "Valor da transação", required = true)
    private BigDecimal valor;
    @Schema(description = "Data da transação", required = true)
    @NotNull
    private LocalDateTime data;
    @NotNull
    private Conta conta;
    @NotNull
    private BeneficiatioDto beneficiario;
    @NotNull
    private TipoTransacao tipoTransacao;
    @Schema(description = "Identificador único da transação", required = true)
    private UUID uui;
    @Schema(description = "Situação da Transação", required = false)
    @NotNull
    private SituacaoEnum situacao;

    public void naoAnalisada() {
        setSituacao(SituacaoEnum.NAO_ANALISADA);
    }

    public void analisada() {
        setSituacao(SituacaoEnum.ANALISADA);
    }

    public void rejeitada() {
        setSituacao(SituacaoEnum.REJEITADA);
    }

    public void suspeitaFraude() {
        setSituacao(SituacaoEnum.EM_SUSPEITA_FRAUDE);
    }

    public void analiseHumana() {
        setSituacao(SituacaoEnum.EM_ANALISE_HUMANA);
    }

    public void aprovada() {
        setSituacao(SituacaoEnum.APROVADA);
    }

    public boolean isAnalisada() {

        return getSituacao().equals(SituacaoEnum.ANALISADA);
    }
}
