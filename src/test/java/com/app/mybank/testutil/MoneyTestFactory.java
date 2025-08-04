package com.app.mybank.testutil;

import com.app.mybank.domain.common.Money;

import java.math.BigDecimal;
import java.util.Currency;

public final class MoneyTestFactory {
    private static final Currency PLN = Currency.getInstance("PLN");

    public static Money pln(String amount) {
        return new Money(new BigDecimal(amount), PLN);
    }

    private MoneyTestFactory() {}
}
