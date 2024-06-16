package ru.leonidm.millida.rating.api;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.millida.rating.api.repository.DeferredRewardRepository;
import ru.leonidm.millida.rating.api.repository.RatingPlayerRepository;
import ru.leonidm.millida.rating.api.repository.StatisticRepository;
import ru.leonidm.millida.rating.api.service.AwardService;
import ru.leonidm.millida.rating.api.service.GuiService;
import ru.leonidm.millida.rating.api.service.HologramsService;
import ru.leonidm.millida.rating.api.service.RatingRequestService;
import ru.leonidm.millida.rating.api.service.RatingRequester;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

public interface MillidaRatingApi {

    @Nullable
    static MillidaRatingApi getNullable() {
        return (MillidaRatingApi) Bukkit.getPluginManager().getPlugin("MillidaRating");
    }

    @NotNull
    static MillidaRatingApi get() {
        return get(NoSuchElementException::new);
    }

    @NotNull
    static <T extends Throwable> MillidaRatingApi get(@NotNull Supplier<T> exception) throws T {
        MillidaRatingApi api = getNullable();
        if (api == null) {
            throw exception.get();
        }

        return api;
    }

    @NotNull
    RatingRequestService getRatingRequestService();

    @NotNull
    DeferredRewardRepository getDeferredRewardRepository();

    @NotNull
    RatingPlayerRepository getRatingPlayerRepository();

    @NotNull
    StatisticRepository getStatisticRepository();

    @NotNull
    AwardService getAwardService();

    @NotNull
    RatingRequester getRatingRequester();

    @NotNull
    GuiService getGuiService();

    @NotNull
    HologramsService getHologramsService();

}
