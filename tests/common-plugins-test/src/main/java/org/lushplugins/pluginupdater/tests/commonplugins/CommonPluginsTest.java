package org.lushplugins.pluginupdater.tests.commonplugins;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.yaml.YamlFormat;
import org.lushplugins.pluginupdater.api.exception.InvalidVersionFormatException;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.cli.PluginUpdaterCLI;
import org.lushplugins.pluginupdater.cli.platform.CLIPlatform;
import org.lushplugins.pluginupdater.cli.plugin.CLIPluginInfo;
import org.lushplugins.pluginupdater.common.config.deserializer.PluginDataDeserializer;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class CommonPluginsTest {

    static {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            UpdaterConstants.LOGGER.severe(sw.toString());
        });
    }

    public static void main(String[] args) {
        PluginUpdaterCLI cli = PluginUpdaterCLI.prepareCLI();

        InputStream resource = cli.getResourceStream("common-plugins.yml");
        Config config = YamlFormat.defaultInstance().createParser().parse(resource);
        List<PluginData> undownloadedPluginData = config.entrySet().stream()
            .map((entry) -> {
                // The following method requires PluginInfo to be available before being run but does not use the
                // current version so we can just supply an empty version
                PluginInfo pluginInfo = new CLIPluginInfo(entry.getKey(), "0.0.0", null);
                Config pluginConfig = config.get(entry.getKey());
                // As we are hard coding the local version we must remove the version-format option
                pluginConfig.remove("version-format");
                return PluginDataDeserializer.deserialize(pluginInfo, pluginConfig);
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(PluginData::pluginName))
            .toList();

        // We fetch all latest versions and then download all jars
        // This is to ensure that each process is handled in batches
        for (PluginData pluginData : undownloadedPluginData) {
            cli.getLogger().info("Fetching %s's latest version"
                .formatted(pluginData.pluginName()));
            try {
                pluginData.latestVersion(pluginData.fetchLatestVersion().version());
            } catch (RuntimeException e) {
                cli.getLogger().severe(e.getMessage());
            }
        }

        Path downloadDir = cli.getPluginsFolder();
        for (PluginData pluginData : undownloadedPluginData) {
            pluginData.downloadUpdate(downloadDir);
        }

        // Platform needs to be created after plugins are downloaded as it collects data from jars on startup
        CLIPlatform platform = new CLIPlatform(cli);
        List<PluginData> commonPluginData = config.entrySet().stream()
            .map((entry) -> {
                String pluginName = entry.getKey();
                PluginInfo pluginInfo = platform.getPlugin(pluginName);
                if (pluginInfo == null) {
                    cli.getLogger().log(Level.WARNING, "Failed to find plugin jar for %s"
                        .formatted(pluginName));
                    return null;
                }

                return PluginDataDeserializer.deserialize(pluginInfo, config.get(pluginName));
            })
            .filter(Objects::nonNull)
            .toList();

        testRemoteVersionParsing(commonPluginData);
    }

    public static void testLocalVersionParsing(List<PluginData> commonPluginData) {

    }

    public static void testRemoteVersionParsing(List<PluginData> commonPluginData) {
        List<String> problemPlugins = new ArrayList<>();
        for (PluginData pluginData : commonPluginData) {
            try {
                pluginData.fetchLatestVersion();
            } catch (InvalidVersionFormatException e) {
                problemPlugins.add(pluginData.pluginName());
            }
        }

        if (!problemPlugins.isEmpty()) {
            throw new RuntimeException("Failed to parse remote version strings for: " + String.join(", ", problemPlugins));
        }
    }
}
