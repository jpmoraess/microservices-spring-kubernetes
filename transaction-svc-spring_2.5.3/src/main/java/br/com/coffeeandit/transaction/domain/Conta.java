package br.com.coffeeandit.transaction.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class Conta implements Serializable {

    private static final long serialVersionUID = 2806412403585360625L;
    @Schema(description = "Código da Agência", required = true)
    private Long codigoAgencia;
    @Schema(description = "Código da Conta Corrente", required = true)
    private Long codigoConta;
}
