package com.app.mybank.application.fx.port;

import com.app.mybank.domain.fx.FxRate;
import com.app.mybank.domain.fx.FxRateId;

import java.util.Optional;

public interface FxRateRepository {

    void save(FxRate rate);

    Optional<FxRate> latest(FxRateId id);
}
