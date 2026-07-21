package org.lushplugins.pluginupdater.api.version;

import org.lushplugins.pluginupdater.api.source.SourceContext;

public record FetchedVersion(Version version, SourceContext sourceContext) {}
