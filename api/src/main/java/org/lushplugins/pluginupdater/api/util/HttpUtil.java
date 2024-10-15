package org.lushplugins.pluginupdater.api.util;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpUtil {

    public static HttpResponse<String> sendRequest(String uri) throws IOException, InterruptedException {
        return sendRequest(URI.create(uri), null);
    }

    public static HttpResponse<String> sendRequest(String uri, @Nullable JsonElement payload) throws IOException, InterruptedException {
        return sendRequest(URI.create(uri), UpdaterConstants.GSON.toJson(payload));
    }

    public static HttpResponse<String> sendRequest(URI uri, @Nullable String payload) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri)
            .header("User-Agent", "PluginUpdater/" + UpdaterConstants.VERSION);

        if (payload != null) {
            requestBuilder
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload));
        } else {
            requestBuilder
                .GET();
        }

        return HttpClient.newHttpClient().send(
            requestBuilder.build(),
            HttpResponse.BodyHandlers.ofString());
    }
}
