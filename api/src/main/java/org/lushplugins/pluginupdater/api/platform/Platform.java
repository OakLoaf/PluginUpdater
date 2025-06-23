package org.lushplugins.pluginupdater.api.platform;

import org.lushplugins.pluginupdater.api.version.VersionChecker;

import java.util.concurrent.Callable;

/**
 * @param rateLimit The endpoint's rate limit per second. Set to {@code -1} to remove limit.
 * @param updater
 * @param platformDataConstructor
 */
public record Platform(
    int rateLimit,
    Callable<VersionChecker> updater,
    PlatformRegistry.PlatformDataConstructor platformDataConstructor
) {
    public boolean hasRateLimit() {
        return this.rateLimit > 0;
    }
}
