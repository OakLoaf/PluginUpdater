package org.lushplugins.pluginupdater.api.http;

import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Endpoint {
    private static final Map<String, Endpoint> endpoints = new HashMap<>();

    private final String url;
    private final RateLimit rateLimit;
    private Long firstRequestEpoch;
    private int requests;
    private Long rateLimitReset;

    private Endpoint(String url, RateLimit rateLimit) {
        this.url = url;
        this.rateLimit = rateLimit;
    }

    public String url() {
        return url;
    }

    public RateLimit rateLimit() {
        return rateLimit;
    }

    public boolean canMakeRequest() {
        if (firstRequestEpoch == null) {
            return true;
        }

        long currentEpoch = Instant.now().getEpochSecond();
        if (currentEpoch - firstRequestEpoch < rateLimit.duration().getSeconds()) {
            firstRequestEpoch = null;
            return true;
        }

        return requests <= rateLimit.requests();
    }

    public void markRequest() {
        this.requests++;

        if (firstRequestEpoch == null) {
            firstRequestEpoch = Instant.now().getEpochSecond();
        }
    }

    public boolean hasRateLimitReset() {
        if (rateLimitReset != null) {
            if (Instant.now().getEpochSecond() <= rateLimitReset) {
                rateLimitReset = null;
                return true;
            } else {
                return false;
            }
        }

        return true;
    }

    public @Nullable Long rateLimitReset() {
        hasRateLimitReset();
        return rateLimitReset;
    }

    public void rateLimitReset(long rateLimitReset) {
        this.rateLimitReset = rateLimitReset;
    }

    public static Optional<Endpoint> endpoint(String endpointUrl) {
        return Optional.ofNullable(endpoints.get(endpointUrl));
    }

    public static Endpoint create(String endpointUrl, RateLimit rateLimit) {
        return endpoints.computeIfAbsent(endpointUrl, (url) -> new Endpoint(url, rateLimit));
    }
}
