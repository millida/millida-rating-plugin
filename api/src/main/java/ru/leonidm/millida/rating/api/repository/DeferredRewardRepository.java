package ru.leonidm.millida.rating.api.repository;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import ru.leonidm.millida.rating.api.entity.DeferredReward;

import java.util.List;
import java.util.UUID;

public interface DeferredRewardRepository {

    void initialize();

    @NotNull
    @Unmodifiable
    List<DeferredReward> getDeferredRewards(@NotNull UUID uuid);

    void addDeferredReward(@NotNull UUID uuid, int day);

    void deleteDeferredReward(@NotNull DeferredReward deferredReward);

    void clear();

    void close();

}
