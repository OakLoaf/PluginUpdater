package org.lushplugins.pluginupdater.cli;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.NonNull;
import org.lushplugins.pluginupdater.api.source.SourceRegistry;
import org.lushplugins.pluginupdater.api.source.type.GeyserSource;
import org.lushplugins.pluginupdater.api.source.type.HangarSource;
import org.lushplugins.pluginupdater.api.source.type.ModrinthSource;
import org.lushplugins.pluginupdater.api.source.type.SpigotSource;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.cli.command.CLICommandHandler;
import org.lushplugins.pluginupdater.cli.platform.CLIPlatform;
import org.lushplugins.pluginupdater.cli.plugin.parser.BukkitInfoParser;
import org.lushplugins.pluginupdater.cli.plugin.parser.InfoParser;
import org.lushplugins.pluginupdater.cli.plugin.parser.VelocityInfoParser;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.collector.CommonPluginCollector;
import org.lushplugins.pluginupdater.common.collector.ModrinthCollector;
import org.lushplugins.pluginupdater.common.platform.UpdaterPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginUpdaterCLI implements UpdaterPlugin {
    private final Platform platform;
    private final Path pluginsFolder;

    public PluginUpdaterCLI() {
        this.platform = Platform.valueOf(System.getProperty("platform", "paper").toUpperCase());
        this.pluginsFolder = Path.of(System.getProperty("plugins-folder", "plugins"));
    }

    public Platform getPlatform() {
        return platform;
    }

    public Path getPluginsFolder() {
        return pluginsFolder;
    }

    @Override
    public Path getDataPath() {
        return Path.of(System.getProperty("data-folder", "PluginUpdater"));
    }

    @Override
    public Path getDownloadDir() {
        String updateFolder = System.getProperty("update-folder", "update");
        return pluginsFolder.resolve(updateFolder);
    }

    @Override
    public InputStream getResourceStream(String path) {
        return this.getClass().getResourceAsStream("/" + path);
    }

    @Override
    public InputStream getResourceStream(PluginInfo pluginInfo, String path) {
        try (JarFile jarFile = new JarFile(pluginInfo.getFile())) {
            JarEntry entry = jarFile.getJarEntry(path);
            if (entry != null) {
                return jarFile.getInputStream(entry);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public @NonNull ComponentLogger getComponentLogger() {
        return UpdaterConstants.LOGGER;
    }

    public static PluginUpdaterCLI prepareCLI() {
        PluginUpdaterCLI cli = new PluginUpdaterCLI();

        String serverVersion = System.getProperty("server-version");
        Platform platform = cli.getPlatform();
        switch (platform) {
            case PAPER, FOLIA -> {
                SourceRegistry.register(new GeyserSource("spigot"));
                SourceRegistry.register(new HangarSource("paper", serverVersion));
                SourceRegistry.register(new ModrinthSource(
                    platform == Platform.FOLIA ? List.of("folia") : List.of("bukkit", "spigot", "paper", "purpur"),
                    serverVersion));
                SourceRegistry.register(new SpigotSource(serverVersion));
            }
            case VELOCITY -> {
                SourceRegistry.register(new GeyserSource("velocity"));
                SourceRegistry.register(new HangarSource("velocity", serverVersion));
                SourceRegistry.register(new ModrinthSource(List.of("velocity"), null));
                SourceRegistry.register(new SpigotSource(null));
            }
        }

        return cli;
    }

    public static void main(String[] args) {
        try {
            PluginUpdaterCLI cli = prepareCLI();

            String commonPluginsFile = "common-plugins/" + (cli.getPlatform() == Platform.VELOCITY ? "velocity.yml" : "paper.yml");
            new UpdaterImpl<>(
                new CLIPlatform(cli),
                cli,
                new CLICommandHandler(),
                List.of(
                    ModrinthCollector::new,
                    updater -> new CommonPluginCollector(updater, commonPluginsFile)
                )
            );
        } catch (Exception e) {
            UpdaterConstants.LOGGER.warn("An error occurred while running the PluginUpdater CLI", e);
        }
    }

    public enum Platform {
        PAPER(BukkitInfoParser.INSTANCE),
        FOLIA(BukkitInfoParser.INSTANCE),
        VELOCITY(VelocityInfoParser.INSTANCE);

        private final InfoParser infoParser;

        Platform(InfoParser infoParser) {
            this.infoParser = infoParser;
        }

        public InfoParser infoParser() {
            return infoParser;
        }
    }
}
