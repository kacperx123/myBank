package com.app.mybank.persistence.fx;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class FxRateId implements Serializable {
    private String baseCurrency;
    private String targetCurrency;
    private LocalDate rateDate;
}
