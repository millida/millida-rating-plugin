package ru.leonidm.millida.rating.repository.player;

import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.api.repository.RatingPlayerRepository;

public final class ProxyRatingPlayerRepository implements RatingPlayerRepository {

    @Delegate
    private RatingPlayerRepository ratingPlayerRepository;

    public ProxyRatingPlayerRepository(@NotNull RatingPlayerRepository ratingPlayerRepository) {
        this.ratingPlayerRepository = ratingPlayerRepository;
    }

    public void setRatingPlayerRepository(@NotNull RatingPlayerRepository ratingPlayerRepository) {
        this.ratingPlayerRepository = ratingPlayerRepository;
    }
}
