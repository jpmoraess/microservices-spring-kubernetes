package br.com.coffeeandit.limites.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table
@Data
@EqualsAndHashCode(of = "id")
public class LimiteDiario {

    @Id
    @SequenceGenerator(name="limite_diario_seq",
            sequenceName="limite_diario_seq",
            allocationSize=1)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long agencia;
    private Long conta;
    private LocalDate data;
    private BigDecimal valor;
}
