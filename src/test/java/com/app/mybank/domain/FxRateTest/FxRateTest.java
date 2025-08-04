package com.app.mybank.domain.FxRateTest;

import com.app.mybank.domain.common.Money;
import com.app.mybank.domain.exception.fx.InvalidRateException;
import com.app.mybank.domain.exception.fx.SameCurrencyException;
import com.app.mybank.domain.fx.FxRate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class FxRateTest {

    private static final Currency PLN = Currency.getInstance("PLN");
    private static final Currency EUR = Currency.getInstance("EUR");

    @Test
    void shouldCreateValidRateAndConvert() {
        FxRate r = new FxRate(PLN, EUR,
                new BigDecimal("0.25"),
                LocalDate.now(),
                LocalDateTime.now());

        Money eur = r.convert(new Money(new BigDecimal(100.00), PLN));
        assertEquals("25.00", eur.amount().toPlainString());
        assertEquals(EUR, eur.currency());
    }

    @Test
    void shouldRejectZeroRate() {
        assertThrows(InvalidRateException.class,
                () -> new FxRate(PLN, EUR, BigDecimal.ZERO,
                        LocalDate.now(), LocalDateTime.now()));
    }

    @Test
    void shouldRejectSameCurrencies() {
        assertThrows(SameCurrencyException.class,
                () -> new FxRate(PLN, PLN, BigDecimal.ONE,
                        LocalDate.now(), LocalDateTime.now()));
    }
}
