package com.app.mybank.persistence.fx;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface SpringDataFxRateRepository extends JpaRepository<FxRateJpaEntity, FxRateId> {

    Optional<FxRateJpaEntity>
    findFirstByIdBaseCurrencyAndIdTargetCurrencyAndIdRateDateLessThanEqualOrderByIdRateDateDesc(
            String baseCurrency, String targetCurrency, LocalDate maxDate
    );
}
