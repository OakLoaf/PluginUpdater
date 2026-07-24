package org.lushplugins.pluginupdater.api.util;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.http.Endpoint;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.util.BuildParameters;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class HttpUtil {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .build();

    public static HttpClient client() {
        return CLIENT;
    }

    public static HttpResponse<String> sendRequest(URI uri, @Nullable String payload) throws IOException, InterruptedException {
        return CLIENT.send(
            prepareRequestBuilder(uri, payload).build(),
            HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> sendRequest(String uri) throws IOException, InterruptedException {
        return sendRequest(URI.create(uri), null);
    }

    public static HttpResponse<String> sendRequest(String uri, @Nullable JsonElement payload) throws IOException, InterruptedException {
        return sendRequest(URI.create(uri), UpdaterConstants.GSON.toJson(payload));
    }

    public static HttpRequest.Builder prepareRequestBuilder(URI uri, @Nullable String payload) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri)
            .header("User-Agent", "PluginUpdater/" + BuildParameters.VERSION);

        if (payload != null) {
            requestBuilder
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload));
        } else {
            requestBuilder
                .GET();
        }

        return requestBuilder;
    }

    public static void validateResponse(Endpoint endpoint, PluginData pluginData, HttpResponse<?> response) throws IllegalStateException {
        switch(response.statusCode()) {
            case 200 -> {}
            case 429 -> {
                long epochSeconds = response.headers()
                    .firstValue("Retry-After")
                    .map(value -> {
                        if (StringUtil.isNumeric(value)) {
                            return Long.parseLong(value) + Instant.now().getEpochSecond();
                        }

                        return ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME)
                            .toInstant()
                            .getEpochSecond();
                    })
                    .orElseGet(() -> response.headers()
                        .firstValueAsLong("X-RateLimit-Reset")
                        .orElse(-1));

                if (epochSeconds > 0) {
                    endpoint.rateLimitReset(epochSeconds);
                }

                throw new IllegalStateException("Hit rate limit for '%s' endpoint, please report this."
                    .formatted(endpoint.url()));
            }
            default -> throw new IllegalStateException("Received invalid response code (%s) whilst checking '%s' for updates."
                .formatted(response.statusCode(), pluginData.pluginName()));
        }
    }
}
