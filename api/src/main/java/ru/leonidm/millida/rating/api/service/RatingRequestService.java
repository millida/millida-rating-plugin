package ru.leonidm.millida.rating.api.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import ru.leonidm.millida.rating.api.entity.TopPlayer;
import ru.leonidm.millida.rating.api.entity.Vote;

import java.util.List;

public interface RatingRequestService {

    @NotNull
    @Unmodifiable
    List<Vote> fetch(int page);

    @NotNull
    @Unmodifiable
    List<TopPlayer> topDay();

    @NotNull
    @Unmodifiable
    List<TopPlayer> topWeek();

    @NotNull
    @Unmodifiable
    List<TopPlayer> topMonth();

}
