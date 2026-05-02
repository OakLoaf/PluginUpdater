package org.lushplugins.pluginupdater.common.command;

import org.lushplugins.pluginupdater.common.command.annotation.CommandPermission;
import org.lushplugins.pluginupdater.common.command.annotation.PluginName;
import org.lushplugins.pluginupdater.common.platform.UpdaterImpl;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

@Command("updater")
@SuppressWarnings("unused")
public record UpdaterCommand(UpdaterImpl instance) {

    @Subcommand("reload")
    @CommandPermission("pluginupdater.reload")
    public String reload() {
        try {
            instance.reloadConfig();
        } catch (Throwable e) {
            instance.getLogger().log(Level.SEVERE, "Caught error whilst reloading: ", e);
            return "&#ff6969Something went wrong whilst reloading the plugin, check the console for errors";
        }

        return "&#b7faa2Successfully reloaded PluginUpdater";
    }

    @Subcommand("check")
    @CommandPermission("pluginupdater.checkupdates")
    public String check(@PluginName String pluginName) {
        instance.getUpdateHandler().queueUpdateCheck(pluginName);

        return "&#b7faa2Successfully queued check for %s".formatted(pluginName);
    }

    @Subcommand("check all")
    @CommandPermission("pluginupdater.checkupdates")
    public String check() {
        AtomicInteger updateCount = new AtomicInteger(0);
        instance.getConfig().getPlugins().forEach(pluginName -> {
            instance.getUpdateHandler().queueUpdateCheck(pluginName);
            updateCount.incrementAndGet();
        });

        return "&#b7faa2Successfully queued checks for %s plugins".formatted(updateCount.get());
    }
}
