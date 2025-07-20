package com.app.mybank.domain.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    private static final Currency PLN = Currency.getInstance("PLN");
    private static final Currency EUR = Currency.getInstance("EUR");

    // ---------- HAPPY PATH ---------- //

    @Test
    @DisplayName("Should create valid object")
    void shouldCreateValidMoney() {
        Money money = new Money(new BigDecimal("12.34"), PLN);
        assertEquals(new BigDecimal("12.34"), money.amount());
        assertEquals(PLN, money.currency());
        assertEquals("12.34 PLN", money.toString());
    }

    @Test
    @DisplayName("Should add 2 values")
    void shouldAddMoney() {
        Money a = new Money(new BigDecimal("10.00"), PLN);
        Money b = new Money(new BigDecimal("2.55"), PLN);

        Money result = a.add(b);

        assertEquals(new BigDecimal("12.55"), result.amount());
        assertEquals(PLN, result.currency());
    }

    @Test
    @DisplayName("should subtract 2 values")
    void shouldSubtractMoney() {
        Money a = new Money(new BigDecimal("10.00"), PLN);
        Money b = new Money(new BigDecimal("3.25"), PLN);

        Money result = a.subtract(b);

        assertEquals(new BigDecimal("6.75"), result.amount());
    }

    @Test
    @DisplayName("should multiply 2 values")
    void shouldMultiplyMoney() {
        Money a = new Money(new BigDecimal("10.00"), PLN);

        Money result = a.multiply(new BigDecimal("1.5"));

        assertEquals(new BigDecimal("15.00"), result.amount());
    }

    // ---------- WALIDACJE / EDGE-CASES ---------- //

    @Nested
    @DisplayName("Constructor validation - ")
    class ConstructorValidation {

        @Test
        @DisplayName("should throw exception when negative value")
        void shouldFailOnNegativeAmount() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Money(new BigDecimal("-1.00"), PLN));
        }

        @Test
        @DisplayName("should throw exception when scale is too big")
        void shouldFailOnScaleTooLarge() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Money(new BigDecimal("1.234"), PLN));
        }
    }

    @Nested
    @DisplayName("validation of arithmetic operations - ")
    class OperationValidation {

        @Test
        @DisplayName("should throw exception when currency mismatch")
        void shouldFailOnAddCurrencyMismatch() {
            Money pln = new Money(new BigDecimal("1.00"), PLN);
            Money eur = new Money(new BigDecimal("1.00"), EUR);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> pln.add(eur)
            );
            assertTrue(ex.getMessage().contains("Currency mismatch"));
        }

        @Test
        @DisplayName("should throw exception when result of subtraction is below 0")
        void shouldFailOnSubtractBelowZero() {
            Money a = new Money(new BigDecimal("5.00"), PLN);
            Money b = new Money(new BigDecimal("6.00"), PLN);

            assertThrows(IllegalArgumentException.class, () -> a.subtract(b));
        }
    }
}
