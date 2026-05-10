package org.lushplugins.pluginupdater.paper.api.plugin;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.source.SourceRegistry;
import org.lushplugins.pluginupdater.api.source.type.ModrinthSource;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public record PaperPluginInfo(Plugin plugin) implements PluginInfo {

    static {
        SourceRegistry.register(new ModrinthSource(List.of(
            "bukkit", "spigot", "paper", "purpur", "folia"
        )));
    }

    @Override
    public String getName() {
        return plugin.getName();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public @Nullable File getFile() {
        try {
            Method method = JavaPlugin.class.getDeclaredMethod("getFile");
            method.setAccessible(true);

            return (File) method.invoke(plugin);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            UpdaterConstants.LOGGER.log(Level.WARNING, "Caught error whilst getting plugin file: ", e);
            return null;
        }
    }

    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }
}
