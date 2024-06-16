package ru.leonidm.millida.rating.service;

import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.api.service.RatingRequestService;

public final class ProxyRatingRequestService implements RatingRequestService {

    @Delegate
    private RatingRequestService ratingRequestService;

    public ProxyRatingRequestService(@NotNull RatingRequestService ratingRequestService) {
        this.ratingRequestService = ratingRequestService;
    }

    public void setRatingRequestService(@NotNull RatingRequestService ratingRequestService) {
        this.ratingRequestService = ratingRequestService;
    }
}
