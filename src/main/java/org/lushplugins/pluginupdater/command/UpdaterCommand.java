package org.lushplugins.pluginupdater.command;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.config.ConfigManager;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

@Command("updater")
@SuppressWarnings("unused")
public class UpdaterCommand {

    @Subcommand("reload")
    @CommandPermission("pluginupdater.reload")
    public String reload() {
        try {
            PluginUpdater.getInstance().getConfigManager().reloadConfig();
        } catch (Throwable e) {
            PluginUpdater.getInstance().getLogger().log(Level.SEVERE, "Caught error whilst reloading: ", e);
            return "&#ff6969Something went wrong whilst reloading the plugin, check the console for errors";
        }

        return "&#b7faa2Successfully reloaded PluginUpdater";
    }

    @Subcommand("runchecks")
    @CommandPermission("pluginupdater.checkupdates")
    public String runChecks() {
        AtomicInteger updateCount = new AtomicInteger(0);
        PluginUpdater.getInstance().getConfigManager().getPlugins().forEach(pluginName -> {
            PluginUpdater.getInstance().getUpdateHandler().queueUpdateCheck(pluginName);
            updateCount.incrementAndGet();
        });

        return "&#b7faa2Successfully queued checks for %s plugins".formatted(updateCount.get());
    }

    @Subcommand("unregisteredplugins")
    @CommandPermission("pluginupdater.unregisteredplugins")
    public String unregisteredPlugins() {
        ConfigManager configManager = PluginUpdater.getInstance().getConfigManager();
        List<String> unregisteredPlugins = Arrays.stream(Bukkit.getPluginManager().getPlugins())
            .map(Plugin::getName)
            .filter(pluginName -> configManager.getPluginData(pluginName) == null)
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .toList();

        if (!unregisteredPlugins.isEmpty()) {
            return "&fUnregistered Plugins (%s):\n&#ff6969%s".formatted(unregisteredPlugins.size(), String.join("&7, &#ff6969", unregisteredPlugins));
        } else {
            return "&#ff6969No unregistered plugins found";
        }
    }
}
