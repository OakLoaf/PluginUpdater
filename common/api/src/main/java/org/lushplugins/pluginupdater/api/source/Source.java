package org.lushplugins.pluginupdater.api.source;

import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.exception.InvalidVersionFormatException;
import org.lushplugins.pluginupdater.api.http.RateLimit;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.version.DownloadableRelease;
import org.lushplugins.pluginupdater.api.version.Version;

import java.io.*;

public interface Source {

    String getName();

    Version fetchLatestVersion(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException, InvalidVersionFormatException;

    DownloadableRelease fetchDownloadableRelease(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException;

    @Nullable String getChangelogUrl(PluginData pluginData, SourceData sourceData);
}
