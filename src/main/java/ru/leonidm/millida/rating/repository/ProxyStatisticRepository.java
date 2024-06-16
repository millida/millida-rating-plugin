package ru.leonidm.millida.rating.repository;

import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.api.repository.StatisticRepository;

public class ProxyStatisticRepository implements StatisticRepository {

    @Delegate
    private StatisticRepository statisticRepository;

    public ProxyStatisticRepository(@NotNull StatisticRepository statisticRepository) {
        this.statisticRepository = statisticRepository;
    }

    public void setStatisticRepository(@NotNull StatisticRepository statisticRepository) {
        this.statisticRepository = statisticRepository;
    }
}
