package org.lushplugins.pluginupdater.cli.platform;

import com.electronwill.nightconfig.core.Config;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.platform.UpdaterPlatform;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.cli.PluginUpdaterCLI;
import org.lushplugins.pluginupdater.cli.plugin.CLIPluginInfo;
import org.lushplugins.pluginupdater.cli.plugin.parser.InfoParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class CLIPlatform implements UpdaterPlatform<Object> {
    private final Map<String, CLIPluginInfo> plugins;

    public CLIPlatform(PluginUpdaterCLI cli) {
        InfoParser infoParser = cli.getPlatform().infoParser();
        try {
            Files.createDirectories(cli.getPluginsFolder());

            this.plugins = Files.list(cli.getPluginsFolder())
                .filter(path -> path.toString().endsWith(".jar"))
                .map(path -> {
                    File file = path.toFile();
                    try (JarFile jarFile = new JarFile(file)) {
                        InfoParser fallbackInfoParser = infoParser;
                        while (fallbackInfoParser != null) {
                            JarEntry entry = jarFile.getJarEntry(fallbackInfoParser.getResourceName());
                            if (entry != null) {
                                try (InputStream input = jarFile.getInputStream(entry)) {
                                    Config config = fallbackInfoParser.parseResource(input);

                                    return new CLIPluginInfo(
                                        fallbackInfoParser.getName(config),
                                        fallbackInfoParser.getRawVersion(config),
                                        file);
                                }
                            }

                            fallbackInfoParser = fallbackInfoParser.getFallbackInfoParser();
                        }
                    } catch (IOException e) {
                        cli.getComponentLogger().error("Failed to interpret jar file for {}", path, e);
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                    CLIPluginInfo::getName,
                    UnaryOperator.identity()
                ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @Nullable CLIPluginInfo getPlugin(String name) {
        return plugins.get(name);
    }

    @Override
    public List<CLIPluginInfo> getPlugins() {
        return new ArrayList<>(plugins.values());
    }

    @Override
    public Collection<Object> getOnlineUsers() {
        return Collections.emptyList();
    }

    @Override
    public List<Object> getOnlineUsersWithPermission(@Nullable String permission) {
        return Collections.emptyList();
    }

    @Override
    public void broadcastActionBar(List<Object> users, String message) {}

    @Override
    public void sendActionBar(Object user, String message) {}

    @Override
    public void broadcastMessage(Collection<Object> users, String message) {
        UpdaterConstants.LOGGER.info(MiniMessage.miniMessage().deserialize(message));
    }

    @Override
    public void sendMessage(Object user, String message) {}

    @Override
    public boolean hasPermission(Object user, String permission) {
        return true;
    }
}
