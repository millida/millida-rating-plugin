package ru.leonidm.millida.rating.api.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import ru.leonidm.millida.rating.api.entity.TopPlayer;

import java.util.List;

public interface RatingRequester {

    void initialize();

    @NotNull
    @Unmodifiable
    List<TopPlayer> getDayTopPlayers();

    @NotNull
    @Unmodifiable
    List<TopPlayer> getWeekTopPlayers();

    @NotNull
    @Unmodifiable
    List<TopPlayer> getMonthTopPlayers();

    void onTopUpdate(@NotNull Runnable runnable);

    void close();

}
