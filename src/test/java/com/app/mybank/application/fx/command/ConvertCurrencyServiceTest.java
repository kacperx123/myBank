package com.app.mybank.application.fx.command;

import com.app.mybank.domain.fx.FxRate;
import com.app.mybank.infastructure.stub.InMemoryFxRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class ConvertCurrencyServiceTest {

    InMemoryFxRateRepository repo;
    ApplicationEventPublisher publisher;
    ConvertCurrencyService svc;

    @BeforeEach
    void init() {
        repo = new InMemoryFxRateRepository();
        publisher = event -> {};
        Clock clk = Clock.fixed(Instant.parse("2025-01-02T00:00:00Z"), ZoneOffset.UTC);
        svc = new ConvertCurrencyService(repo, publisher, clk);



        repo.save(new FxRate(Currency.getInstance("PLN"),
                Currency.getInstance("EUR"),
                new BigDecimal("0.25"),
                LocalDate.of(2025,1,2),
                Instant.now(clk).atZone(ZoneOffset.UTC).toLocalDateTime()));
    }

    @Test
    void shouldReturnLatestFxRate() {
        FxRate r = svc.getLatest(Currency.getInstance("PLN"), Currency.getInstance("EUR"));
        assertEquals("0.25", r.rate().toPlainString());
    }
}
