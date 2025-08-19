package com.app.mybank.persistence.fx;

import com.app.mybank.application.fx.port.FxRateRepository;
import com.app.mybank.domain.fx.FxRate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Currency;
import java.util.Optional;

@Component
@Transactional
public class FxRateJpaAdapter implements FxRateRepository {

    private final SpringDataFxRateRepository repo;

    public FxRateJpaAdapter(SpringDataFxRateRepository repo) {
        this.repo = repo;
    }

    @Override
    public void save(FxRate rate) {
        var id = new FxRateId(
                rate.base().getCurrencyCode(),
                rate.target().getCurrencyCode(),
                rate.rateDate()              // ważne: w DB mamy rate_date (DATE)
        );
        var entity = FxRateJpaEntity.builder()
                .id(id)
                .rate(rate.rate())
                .cachedAt(rate.cachedAt())
                .build();
        repo.save(entity);
    }

    @Override
    public Optional<FxRate> latest(com.app.mybank.domain.fx.FxRateId id) {
        return repo.findFirstByIdBaseCurrencyAndIdTargetCurrencyAndIdRateDateLessThanEqualOrderByIdRateDateDesc(
                        id.base().getCurrencyCode(), id.target().getCurrencyCode(), id.rateDate()
                )
                .map(this::toDomain);
    }

    private FxRate toDomain(FxRateJpaEntity e) {
        return new FxRate(
                Currency.getInstance(e.getId().getBaseCurrency()),
                Currency.getInstance(e.getId().getTargetCurrency()),
                e.getRate(),
                e.getId().getRateDate(),
                e.getCachedAt()
        );
    }
}
