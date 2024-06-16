package ru.leonidm.millida.rating.service;

import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.api.service.AwardService;

public final class ProxyAwardService implements AwardService {

    @Delegate
    private AwardService awardService;

    public ProxyAwardService(@NotNull AwardService awardService) {
        this.awardService = awardService;
    }

    public void setAwardService(@NotNull AwardService awardService) {
        this.awardService = awardService;
    }
}
