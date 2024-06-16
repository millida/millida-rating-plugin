package ru.leonidm.millida.rating.service;

import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import ru.leonidm.millida.rating.api.entity.TopPlayer;
import ru.leonidm.millida.rating.api.entity.Vote;
import ru.leonidm.millida.rating.api.service.RatingRequestService;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

public class MillidaRatingRequestService implements RatingRequestService {

    private static final Type VOTES_TYPE = new TypeToken<List<Vote>>() {}.getType();
    private static final Type TOP_PLAYERS_TYPE = new TypeToken<List<TopPlayer>>() {}.getType();

    private final Logger logger = Logger.getLogger("MillidaRating");
    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();
    private final HttpService httpService = new HttpService(gson, logger);
    private final String baseUrl;

    public MillidaRatingRequestService(int serverId) {
        this("https://rating.millida.net/api/votes/" + serverId + "/");
    }

    public MillidaRatingRequestService(@NotNull String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    @Override
    @NotNull
    @Unmodifiable
    public List<Vote> fetch(int page) {
        return get(baseUrl + "page/" + page, httpService::getJsonArray).map(jsonArray -> {
            return (List<Vote>) gson.fromJson(jsonArray, VOTES_TYPE);
        }).orElse(Collections.emptyList());
    }

    @Override
    @NotNull
    @Unmodifiable
    public List<TopPlayer> topDay() {
        return top("d");
    }

    @Override
    @NotNull
    @Unmodifiable
    public List<TopPlayer> topWeek() {
        return top("w");
    }

    @Override
    @NotNull
    @Unmodifiable
    public List<TopPlayer> topMonth() {
        return top("m");
    }

    @NotNull
    @Unmodifiable
    private List<TopPlayer> top(@NotNull String letter) {
        return get(baseUrl + "top/1" + letter, httpService::getJsonArray).map(jsonArray -> {
            return (List<TopPlayer>) gson.fromJson(jsonArray, TOP_PLAYERS_TYPE);
        }).orElse(Collections.emptyList());
    }

    @NotNull
    private <T> Optional<T> get(@NotNull String rawUrl, @NotNull Function<URL, Optional<T>> function) {
        try {
            URL url = new URL(rawUrl);
            return function.apply(url);
        } catch (MalformedURLException e) {
            logger.severe(() -> "Got malformed URL'" + rawUrl + "'");
            return Optional.empty();
        }
    }
}
