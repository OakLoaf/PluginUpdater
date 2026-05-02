package org.lushplugins.pluginupdater.paper.api.plugin;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public record PaperPluginInfo(Plugin plugin) implements PluginInfo {
    private static final Logger LOGGER = Logger.getLogger("PluginUpdater");

    @Override
    public String getName() {
        return plugin.getName();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public File getFile() {
        try {
            Method method = JavaPlugin.class.getDeclaredMethod("getFile");
            method.setAccessible(true);

            return (File) method.invoke(plugin);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.log(Level.WARNING, "[PluginUpdater] Caught error whilst getting plugin file: ", e);
            return null;
        }
    }

    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }
}
