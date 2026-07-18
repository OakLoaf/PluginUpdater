package org.lushplugins.pluginupdater.common.config.deserializer;

import com.electronwill.nightconfig.core.Config;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.source.type.*;
import org.lushplugins.pluginupdater.common.platform.UpdaterPlatform;
import org.lushplugins.pluginupdater.common.util.ConfigUtil;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class SourceDataDeserializer {

    public static SourceData deserialize(UpdaterPlatform platform, Config config) {
        String source = ConfigUtil.getOrAlias(config, "source", "platform",
            () -> platform.getLogger().log(Level.WARNING, "Deprecated: The config option 'updater' has been renamed to 'source'"));

        return switch (source) {
            case GeyserSource.NAME -> geyserSourceData(config);
            case GithubSource.NAME -> githubSourceData(config);
            case HangarSource.NAME -> hangarSourceData(config);
            case ModrinthSource.NAME -> modrinthSourceData(config);
            case SpigotSource.NAME -> spigotSourceData(config);
            default -> null;
        };
    }

    public static GeyserSource.Data geyserSourceData(Config config) {
        return new GeyserSource.Data(
            config.get("project-name")
        );
    }

    public static GithubSource.Data githubSourceData(Config config) {
        return new GithubSource.Data(
            config.get("github-repo"),
            config.get("token"),
            config.get("asset-name")
        );
    }

    public static HangarSource.Data hangarSourceData(Config config) {
        return new HangarSource.Data(
            config.get("hangar-project-slug")
        );
    }

    public static ModrinthSource.Data modrinthSourceData(Config config) {
        List<String> loaders;
        if (ConfigUtil.isOfType(config, "loaders", String.class)) {
            loaders = Collections.singletonList(config.<String>get("loaders").toLowerCase());
        } else if (ConfigUtil.isOfType(config, "loaders", List.class)) {
            loaders = config.<List<String>>get("loaders").stream()
                .map(String::toLowerCase)
                .toList();
        } else {
            loaders = null;
        }

        List<String> releaseChannels;
        if (ConfigUtil.isOfType(config, "channels", String.class)) {
            releaseChannels = Collections.singletonList(config.getOrElse("channels", ModrinthSource.ReleaseChannel.RELEASE).toLowerCase());
        } else if (ConfigUtil.isOfType(config, "channels", List.class)) {
            releaseChannels = config.<List<String>>get("channels").stream()
                .map(String::toLowerCase)
                .toList();
        } else {
            releaseChannels = ModrinthSource.ReleaseChannel.ALL;
        }

        return new ModrinthSource.Data(
            config.get("modrinth-project-id"),
            loaders,
            releaseChannels
        );
    }

    public static SpigotSource.Data spigotSourceData(Config config) {
        return new SpigotSource.Data(
            config.get("spigot-resource-id").toString()
        );
    }
}
