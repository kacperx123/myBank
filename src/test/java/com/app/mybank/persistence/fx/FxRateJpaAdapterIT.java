package com.app.mybank.persistence.fx;

import com.app.mybank.domain.fx.FxRate;
import com.app.mybank.domain.fx.FxRateId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({FxRateJpaAdapter.class})
class FxRateJpaAdapterIT {

    @Autowired
    private FxRateJpaAdapter adapter;

    private final Currency USD = Currency.getInstance("USD");
    private final Currency EUR = Currency.getInstance("EUR");

    @Test
    void shouldSaveAndRetrieveFxRate() {
        FxRate rate = new FxRate(
                USD, EUR,
                new BigDecimal("4.123456"),
                LocalDate.of(2025, 8, 18),
                LocalDateTime.now()
        );

        adapter.save(rate);

        var id = new FxRateId(USD, EUR, LocalDate.of(2025, 8, 18));
        var found = adapter.latest(id);

        assertThat(found).isPresent();
        assertThat(found.get().rate()).isEqualByComparingTo("4.123456");
    }

    @Test
    void shouldReturnMostRecentRateBeforeOrEqualDate() {
        LocalDate today = LocalDate.now();

        adapter.save(new FxRate(USD, EUR, new BigDecimal("4.10"),
                today.minusDays(2), LocalDateTime.now().minusDays(2)));
        adapter.save(new FxRate(USD, EUR, new BigDecimal("4.20"),
                today.minusDays(1), LocalDateTime.now().minusDays(1)));
        adapter.save(new FxRate(USD, EUR, new BigDecimal("4.30"),
                today, LocalDateTime.now()));

        var idToday = new FxRateId(USD, EUR, today);
        var foundToday = adapter.latest(idToday);

        assertThat(foundToday).isPresent()
                .get().extracting(FxRate::rate).isEqualTo(new BigDecimal("4.30"));

        var idYesterday = new FxRateId(USD, EUR, today.minusDays(1));
        var foundYesterday = adapter.latest(idYesterday);

        assertThat(foundYesterday).isPresent()
                .get().extracting(FxRate::rate).isEqualTo(new BigDecimal("4.20"));
    }

    @Test
    void shouldReturnEmptyWhenNoRateExists() {
        var id = new FxRateId(USD, EUR, LocalDate.now());
        var found = adapter.latest(id);

        assertThat(found).isEmpty();
    }
}
