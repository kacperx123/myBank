package com.app.mybank.application.fx.command;

import com.app.mybank.application.fx.port.FxRateRepository;
import com.app.mybank.domain.exception.fx.InvalidRateException;
import com.app.mybank.domain.fx.FxRate;
import com.app.mybank.domain.fx.FxRateId;
import com.app.mybank.domain.fx.events.FxRateCached;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;

@RequiredArgsConstructor
public class ConvertCurrencyService {

    private final FxRateRepository repo;
    private final ApplicationEventPublisher publisher;
    private final Clock clock;

    public FxRate getLatest(Currency base, Currency target) {

        FxRate rate = repo.latest(new FxRateId(base, target, LocalDate.now(clock)))
                .orElseThrow(InvalidRateException::new);

        publisher.publishEvent(new FxRateCached(rate, Instant.now(clock)));

        return rate;
    }
}