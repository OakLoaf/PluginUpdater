package org.lushplugins.pluginupdater.api.http;

import java.time.Duration;

public record RateLimit(int requests, Duration duration) {}
