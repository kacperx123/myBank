package com.app.mybank.infastructure.stub;

import com.app.mybank.application.fx.port.FxRateRepository;
import com.app.mybank.domain.fx.FxRate;
import com.app.mybank.domain.fx.FxRateId;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("test")
public class InMemoryFxRateRepository implements FxRateRepository {

    private final Map<FxRateId, FxRate> store = new ConcurrentHashMap<>();

    @Override
    public void save(FxRate rate) {
        store.put(new FxRateId(rate.base(), rate.target(), rate.rateDate()), rate);
    }

    @Override
    public Optional<FxRate> latest(FxRateId id) {
        return store.values().stream()
                .filter(r -> r.base().equals(id.base())
                        && r.target().equals(id.target())
                        && !r.rateDate().isAfter(id.rateDate()))
                .max(Comparator.comparing(FxRate::rateDate));
    }

    public void clear() { store.clear(); }
}
