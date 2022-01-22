package br.com.coffeeandit.transaction.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "Objeto de transporte para  alteração de uma situacao de transação")
public class AlteracaoSituacaoDTO {

    @Schema(description = "Situação da Transação", required = false)
    private SituacaoEnum situacao;
}
