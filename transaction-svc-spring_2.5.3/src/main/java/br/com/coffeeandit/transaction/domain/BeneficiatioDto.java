package br.com.coffeeandit.transaction.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class BeneficiatioDto implements Serializable {

    private static final long serialVersionUID = 2806421543985360625L;

    @Schema(description = "CPF do Beneficiário", required = true, example = "00337786583")
    private Long CPF;
    @Schema(description = "Código do Banco do Beneficiário", required = true, example = "341")
    private Long codigoBanco;
    @Schema(description = "Código da Agência do Beneficiário", required = true, example = "07649")
    private String agencia;
    @Schema(description = "Número da Conta do Beneficiário", required = true, example = "07649")
    private String conta;
    @Schema(description = "Nome do favorecido do Beneficiário", required = true, example = "João da Silva")
    private String nomeFavorecido;

}
