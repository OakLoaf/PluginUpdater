package org.lushplugins.pluginupdater.command;

import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.util.lamp.annotation.PluginName;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

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

    @Subcommand("check")
    @CommandPermission("pluginupdater.checkupdates")
    public String check(@PluginName String pluginName) {
        PluginUpdater.getInstance().getUpdateHandler().queueUpdateCheck(pluginName);

        return "&#b7faa2Successfully queued check for %s".formatted(pluginName);
    }

    @Subcommand("check all")
    @CommandPermission("pluginupdater.checkupdates")
    public String check() {
        AtomicInteger updateCount = new AtomicInteger(0);
        PluginUpdater.getInstance().getConfigManager().getPlugins().forEach(pluginName -> {
            PluginUpdater.getInstance().getUpdateHandler().queueUpdateCheck(pluginName);
            updateCount.incrementAndGet();
        });

        return "&#b7faa2Successfully queued checks for %s plugins".formatted(updateCount.get());
    }
}
