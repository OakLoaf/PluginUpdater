package org.lushplugins.pluginupdater.api.version;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

public record DownloadableRelease(
    String downloadUrl,
    @Nullable Map<String, String> downloadHeaders,
    @Nullable String jarName
) {

    @Override
    public Map<String, String> downloadHeaders() {
        return downloadHeaders != null ? downloadHeaders : Collections.emptyMap();
    }
}
