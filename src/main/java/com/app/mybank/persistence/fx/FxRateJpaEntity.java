package com.app.mybank.persistence.fx;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fx_rate")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FxRateJpaEntity {

    @EmbeddedId
    private FxRateId id;

    @Column(name = "rate", precision = 18, scale = 6, nullable = false)
    private BigDecimal rate;

    @Column(name = "cached_at", nullable = false)
    private LocalDateTime cachedAt;
}
