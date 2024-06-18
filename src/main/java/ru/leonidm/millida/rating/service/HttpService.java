package ru.leonidm.millida.rating.service;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Logger;

public class HttpService {

    private final Gson gson;
    private final Logger logger;

    public HttpService(@NotNull Gson gson, @NotNull Logger logger) {
        this.gson = gson;
        this.logger = logger;
    }

    @NotNull
    public Optional<JsonArray> getJsonArray(@NotNull URL url, boolean warn) {
        return getJson(url).map(jsonElement -> {
            if (jsonElement instanceof JsonArray) {
                return (JsonArray) jsonElement;
            } else {
                if (warn) {
                    logger.warning("'" + url + "' returned non-array. JSON response: " + jsonElement);
                }
                return null;
            }
        });
    }

    @NotNull
    public Optional<JsonElement> getJson(@NotNull URL url) {
        return get(url).map(response -> {
            try {
                return gson.fromJson(response, JsonElement.class);
            } catch (JsonSyntaxException e) {
                logger.warning("'" + url + "' returned malformed JSON: " + e.getMessage() +
                        ". Response: " + response);
                return null;
            }
        });
    }

    @NotNull
    public Optional<String> get(@NotNull URL url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int status = connection.getResponseCode();
            ByteSource byteSource = new ByteSource() {
                @Override
                public InputStream openStream() throws IOException {
                    return status == 200 ? connection.getInputStream() : connection.getErrorStream();
                }
            };

            String response = byteSource.asCharSource(Charsets.UTF_8).read();

            if (status != 200) {
                logger.warning("'" + url + "' returned " + status + " code. Response: " + response);
                return Optional.empty();
            }

            return Optional.of(response);
        } catch (IOException e) {
            logger.severe("Got exception while processing '" + url + "'");
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
