package org.lushplugins.pluginupdater.cli.platform;

import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.platform.UpdaterPlatform;
import org.lushplugins.pluginupdater.cli.PluginUpdaterCLI;
import org.lushplugins.pluginupdater.cli.plugin.CLIPluginInfo;
import org.lushplugins.pluginupdater.cli.plugin.parser.InfoParser;

import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CLIPlatform implements UpdaterPlatform<Object> {
    private final Map<String, CLIPluginInfo> plugins;

    public CLIPlatform(PluginUpdaterCLI cli) {
        // TODO: Load plugin info from jars and map name to info
        Path pluginsFolder = cli.getPluginsFolder();

        // Get plugin yml from plugin jar and then parse to CLIPluginInfo
        InfoParser infoParser = cli.getPlatform().infoParser();

        this.plugins = Collections.emptyMap();
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
        // TODO: Strip adventure formatting tags
        Logger.getGlobal().log(Level.INFO, message);
    }

    @Override
    public void sendMessage(Object user, String message) {}

    @Override
    public boolean hasPermission(Object user, String permission) {
        return true;
    }
}
