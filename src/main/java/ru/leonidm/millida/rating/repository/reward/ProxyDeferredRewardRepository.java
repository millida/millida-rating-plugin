package ru.leonidm.millida.rating.repository.reward;

import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.api.repository.DeferredRewardRepository;

public final class ProxyDeferredRewardRepository implements DeferredRewardRepository {

    @Delegate
    private DeferredRewardRepository deferredRewardRepository;

    public ProxyDeferredRewardRepository(@NotNull DeferredRewardRepository deferredRewardRepository) {
        this.deferredRewardRepository = deferredRewardRepository;
    }

    public void setDeferredRewardRepository(@NotNull DeferredRewardRepository deferredRewardRepository) {
        this.deferredRewardRepository = deferredRewardRepository;
    }

}
