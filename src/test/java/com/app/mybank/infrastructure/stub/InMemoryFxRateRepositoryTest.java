package com.app.mybank.infrastructure.stub;

import com.app.mybank.domain.fx.FxRate;
import com.app.mybank.domain.fx.FxRateId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryFxRateRepositoryTest {

    InMemoryFxRateRepository repo;

    @BeforeEach
    void init() { repo = new InMemoryFxRateRepository(); }

    @Test
    void shouldReturnLatestRate() {
        Currency PLN = Currency.getInstance("PLN");
        Currency EUR = Currency.getInstance("EUR");
        LocalDate d1 = LocalDate.of(2025,1,1);
        LocalDate d2 = LocalDate.of(2025,1,2);

        repo.save(new FxRate(PLN, EUR, new BigDecimal("0.24"), d1, LocalDateTime.now()));
        repo.save(new FxRate(PLN, EUR, new BigDecimal("0.25"), d2, LocalDateTime.now()));

        var latest = repo.latest(new FxRateId(PLN, EUR, d2))
                .orElseThrow();
        assertEquals("0.25", latest.rate().toPlainString());
    }
}
