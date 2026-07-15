package org.lushplugins.pluginupdater.cli;

import org.apache.commons.cli.*;
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PluginUpdaterCLI implements UpdaterPlugin {
    private Platform platform = Platform.PAPER;
    private Path pluginsFolder = null;

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public Path getPluginsFolder() {
        return pluginsFolder;
    }

    public void setPluginsFolder(Path pluginsFolder) {
        this.pluginsFolder = pluginsFolder;
    }

    @Override
    public Path getDataPath() {
        return pluginsFolder.resolve("PluginUpdater");
    }

    @Override
    public Path getDownloadDir() {
        // TODO: Support getting folder name from system property
        return pluginsFolder.resolve("update");
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
    public Logger getLogger() {
        return Logger.getGlobal();
    }

    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();

        PluginUpdaterCLI cli = new PluginUpdaterCLI();

        try {
            CommandLine cmd = parser.parse(createOptions(), args, false);

            if (cmd.hasOption("p")) {
                cli.setPlatform(Platform.valueOf(cmd.getOptionValue("p").toUpperCase()));
            }

            if (cmd.hasOption("f")) {
                cli.setPluginsFolder(Path.of(cmd.getOptionValue("f")));
            }

            String serverVersion = null;
            if (cmd.hasOption("v")) {
                serverVersion = cmd.getOptionValue("v");
            }

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
        } catch (ParseException e) {
            Logger.getGlobal().log(Level.SEVERE, "Failed to parse arguments", e);
            System.exit(1);
        } catch (RuntimeException e) {
            Logger.getGlobal().log(Level.SEVERE, "Caught an unexpected error", e);
            System.exit(1);
        }
    }

    private static Options createOptions() {
        Options options = new Options();

        return options;
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
