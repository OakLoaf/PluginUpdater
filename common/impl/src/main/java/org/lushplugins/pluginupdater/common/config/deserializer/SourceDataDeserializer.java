package org.lushplugins.pluginupdater.common.config.deserializer;

import com.electronwill.nightconfig.core.Config;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.source.type.*;
import org.lushplugins.pluginupdater.common.platform.UpdaterPlugin;
import org.lushplugins.pluginupdater.common.util.ConfigUtil;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class SourceDataDeserializer {

    public static SourceData deserialize(UpdaterPlugin plugin, Config config) {
        String source = ConfigUtil.getOrAlias(config, "source", "platform",
            () -> plugin.getLogger().log(Level.WARNING, "Deprecated: The config option 'updater' has been renamed to 'source'"));

        return switch (source) {
            case GeyserSource.NAME -> geyserSourceData(config);
            case GithubSource.NAME -> githubSourceData(config);
            case HangarSource.NAME -> hangarSourceData(config);
            case JenkinsSource.NAME -> jenkinsSourceData(config);
            case ModrinthSource.NAME -> modrinthSourceData(config);
            case SpigotSource.NAME -> spigotSourceData(config);
            default -> null;
        };
    }

    public static GeyserSource.Data geyserSourceData(Config config) {
        return GeyserSource.Data.builder()
            .projectName(config.get("project-name"))
            .build();
    }

    public static GithubSource.Data githubSourceData(Config config) {
        return GithubSource.Data.builder()
            .repo(config.get("github-repo"))
            .token(config.get("token"))
            .assetName(config.get("asset-name"))
            .build();
    }

    public static HangarSource.Data hangarSourceData(Config config) {
        return HangarSource.Data.builder()
            .projectSlug(config.get("hangar-project-slug"))
            .build();
    }

    public static JenkinsSource.Data jenkinsSourceData(Config config) {
        return JenkinsSource.Data.builder()
            .url(config.get("url"))
            .job(config.get("job"))
            .artifactName(config.get("artifact-name"))
            .build();
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

        return ModrinthSource.Data.builder()
            .projectId(config.get("modrinth-project-id"))
            .loaders(loaders)
            .releaseChannels(releaseChannels)
            .build();
    }

    public static SpigotSource.Data spigotSourceData(Config config) {
        return SpigotSource.Data.builder()
            .resourceId(config.get("spigot-resource-id"))
            .build();
    }
}
