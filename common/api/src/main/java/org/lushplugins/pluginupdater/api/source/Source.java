package org.lushplugins.pluginupdater.api.source;

import org.lushplugins.pluginupdater.api.version.VersionChecker;

import java.util.concurrent.Callable;

/**
 * @param rateLimit The endpoint's rate limit per second, set to {@code -1} to remove limit
 */
public record Source(
    int rateLimit,
    Callable<VersionChecker> updater,
    SourceRegistry.SourceDataConstructor sourceDataConstructor
) {

    public boolean hasRateLimit() {
        return this.rateLimit > 0;
    }
}
