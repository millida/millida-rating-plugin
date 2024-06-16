package ru.leonidm.millida.rating.api.repository;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.millida.rating.api.entity.RatingPlayer;

import java.util.UUID;

public interface RatingPlayerRepository {

    void initialize();

    @Nullable
    RatingPlayer findRatingPlayer(@NotNull UUID uuid);

    @Nullable
    RatingPlayer createRatingPlayer(@NotNull UUID uuid);

    void saveRatingPlayer(@NotNull RatingPlayer ratingPlayer);

    void deleteRatingPlayer(@NotNull UUID uuid);

    void clear();

    void close();

}
