package org.lushplugins.pluginupdater.paper.api.plugin;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public record PaperPluginInfo(Plugin plugin) implements PluginInfo {

    @Override
    public String getName() {
        return plugin.getName();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public @Nullable File getFile() {
        try {
            Method method = JavaPlugin.class.getDeclaredMethod("getFile");
            method.setAccessible(true);

            return (File) method.invoke(plugin);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            UpdaterConstants.LOGGER.warn("Caught error whilst getting plugin file: ", e);
            return null;
        }
    }

    @Override
    public ComponentLogger getComponentLogger() {
        return plugin.getComponentLogger();
    }
}
