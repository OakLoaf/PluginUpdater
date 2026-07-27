package org.lushplugins.pluginupdater.cli;

import org.lushplugins.pluginupdater.api.source.SourceRegistry;
import org.lushplugins.pluginupdater.api.source.type.GeyserSource;
import org.lushplugins.pluginupdater.api.source.type.HangarSource;
import org.lushplugins.pluginupdater.api.source.type.ModrinthSource;
import org.lushplugins.pluginupdater.api.source.type.SpigotSource;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.cli.command.CLICommandHandler;
import org.lushplugins.pluginupdater.cli.platform.CLIPlatform;
import org.lushplugins.pluginupdater.cli.plugin.parser.BukkitInfoParser;
import org.lushplugins.pluginupdater.cli.plugin.parser.InfoParser;
import org.lushplugins.pluginupdater.cli.plugin.parser.VelocityInfoParser;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.collector.ModrinthCollector;
import org.lushplugins.pluginupdater.common.platform.UpdaterPlugin;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

public class PluginUpdaterCLI implements UpdaterPlugin {
    private Platform platform = null;
    private String serverVersion = null;
    private Path pluginsFolder = null;

    public Platform getPlatform() {
        return platform;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public Path getPluginsFolder() {
        return pluginsFolder;
    }

    @Override
    public Path getDataPath() {
        // TODO
        return null;
    }

    @Override
    public Path getDownloadDir() {
        // TODO
        return null;
    }

    @Override
    public InputStream getResourceStream(String path) {
        return this.getClass().getResourceAsStream("/" + path);
    }

    @Override
    public InputStream getResourceStream(PluginInfo pluginInfo, String path) {
        // TODO: get resource from jar file?
        return null;
    }

    @Override
    public Logger getLogger() {
        return Logger.getGlobal();
    }

    public static void main(String[] args) {
        PluginUpdaterCLI cli = new PluginUpdaterCLI();

        String serverVersion = cli.getServerVersion();
        switch (cli.getPlatform()) {
            case PAPER -> {
                SourceRegistry.register(new GeyserSource("spigot"));
                SourceRegistry.register(new HangarSource("paper", serverVersion));
                SourceRegistry.register(new ModrinthSource(List.of("bukkit", "spigot", "paper", "purpur"), serverVersion));
                SourceRegistry.register(new SpigotSource(serverVersion));
            }
            case FOLIA -> {
                SourceRegistry.register(new GeyserSource("spigot"));
                SourceRegistry.register(new HangarSource("paper", serverVersion));
                SourceRegistry.register(new ModrinthSource(List.of("folia"), serverVersion));
                SourceRegistry.register(new SpigotSource(serverVersion));
            }
            case VELOCITY -> {
                SourceRegistry.register(new GeyserSource("velocity"));
                SourceRegistry.register(new HangarSource("velocity", serverVersion));
                SourceRegistry.register(new ModrinthSource(List.of("velocity"), null));
                SourceRegistry.register(new SpigotSource(null));
            }
        }

        UpdaterImpl<?> updater = new UpdaterImpl<>(
            new CLIPlatform(cli),
            cli,
            new CLICommandHandler(),
            List.of(
                ModrinthCollector::new
            )
        );
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
