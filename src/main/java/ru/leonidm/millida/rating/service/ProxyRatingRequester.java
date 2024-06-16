package ru.leonidm.millida.rating.service;

import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.api.service.RatingRequester;

public final class ProxyRatingRequester implements RatingRequester {

    @Delegate
    private RatingRequester ratingRequester;

    public ProxyRatingRequester(@NotNull RatingRequester ratingRequester) {
        this.ratingRequester = ratingRequester;
    }

    public void setRatingRequester(@NotNull RatingRequester ratingRequester) {
        this.ratingRequester = ratingRequester;
    }
}
