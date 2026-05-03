package org.lushplugins.pluginupdater.common.config.serializer;

import com.electronwill.nightconfig.core.Config;
import org.lushplugins.pluginupdater.api.source.type.GithubSource;
import org.lushplugins.pluginupdater.api.source.type.HangarSource;
import org.lushplugins.pluginupdater.api.source.type.ModrinthSource;
import org.lushplugins.pluginupdater.api.source.type.SpigotSource;
import org.lushplugins.pluginupdater.common.util.ConfigUtil;

import java.util.Collections;
import java.util.List;

public class SourceDataDeserializer {

    public static GithubSource.Data githubSourceData(Config config) {
        return new GithubSource.Data(
            config.get("github-repo"),
            config.get("token")
        );
    }

    public static HangarSource.Data hangarSourceData(Config config) {
        return new HangarSource.Data(
            config.get("hangar-project-slug")
        );
    }

    public static ModrinthSource.Data modrinthSourceData(Config config) {
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
            releaseChannels
        );
    }

    public static SpigotSource.Data spigotSourceData(Config config) {
        return new SpigotSource.Data(
            config.get("spigot-resource-id")
        );
    }
}
