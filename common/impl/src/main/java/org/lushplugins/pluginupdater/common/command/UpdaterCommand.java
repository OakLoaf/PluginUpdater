package org.lushplugins.pluginupdater.common.command;

import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.common.command.annotation.CommandPermission;
import org.lushplugins.pluginupdater.common.command.annotation.PluginName;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.updater.UpdateHandler;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.orphan.OrphanCommand;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

@SuppressWarnings("unused")
public record UpdaterCommand(UpdaterImpl<?> updater) implements OrphanCommand {

    @Subcommand("reload")
    @CommandPermission("pluginupdater.reload")
    public String reload() {
        try {
            updater.config().reload();
        } catch (Throwable e) {
            updater.updaterPlugin().getLogger().log(Level.SEVERE, "Caught error whilst reloading: ", e);
            return "<#ff6969>Something went wrong whilst reloading the plugin, check the console for errors";
        }

        return "<#b7faa2>Successfully reloaded PluginUpdater";
    }

    @Subcommand("check")
    @CommandPermission("pluginupdater.checkupdates")
    public String check(CommandActor actor, @PluginName String pluginName) {
        UpdateHandler.ProcessingData processingData = updater.updateHandler().queueUpdateCheck(pluginName);

        processingData.getFuture().thenAccept(success -> {
            PluginData pluginData = processingData.getPluginData();
            switch (pluginData.versionDifference()) {
                case MAJOR, MINOR, PATCH, BUILD -> updater.commandPlatform().sendMessage(actor, "<#b7faa2>New version <#b7faa2>found for %s <white>(%s <gray>-> <white>%s)"
                    .formatted(pluginData.pluginName(),
                        pluginData.currentVersion().rawVersionString(),
                        pluginData.latestVersion().orElseThrow().rawVersionString()));
                case LATEST -> updater.commandPlatform().sendMessage(actor, ("<#b7faa2>No update has been found for %s")
                    .formatted(pluginData.pluginName()));
                case UNKNOWN -> updater.commandPlatform().sendMessage(actor, "<#ff6969>Something went wrong when checking %s for a new version"
                    .formatted(pluginData.pluginName()));
            }
        });

        return "<#b7faa2>Successfully queued check for %s".formatted(pluginName);
    }

    @Subcommand("check all")
    @CommandPermission("pluginupdater.checkupdates")
    public String check() {
        AtomicInteger updateCount = new AtomicInteger(0);
        updater.config().getPlugins().forEach(pluginName -> {
            updater.updateHandler().queueUpdateCheck(pluginName);
            updateCount.incrementAndGet();
        });
        updater.updateHandler().queueBroadcastNotification();

        return "<#b7faa2>Successfully queued checks for %s plugins".formatted(updateCount.get());
    }

    @Subcommand("updates")
    @CommandPermission("pluginupdater.checkupdates")
    public String updates() {
        return UpdatesCommand.updates(updater);
    }

    @Subcommand("list updates")
    @CommandPermission("pluginupdater.checkupdates")
    public String listUpdates() {
        return UpdatesCommand.listUpdates(updater);
    }

    @Subcommand("list unregistered")
    @CommandPermission("pluginupdater.unregisteredplugins")
    public String listUnregisteredPlugins() {
        List<String> unregisteredPlugins = updater.platform().getPlugins().stream()
            .map(PluginInfo::getName)
            .filter(pluginName -> updater.config().canRegisterPluginData(pluginName))
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .toList();

        if (!unregisteredPlugins.isEmpty()) {
            return "<white>Unregistered Plugins (%s):\n<#ff6969>%s".formatted(unregisteredPlugins.size(), String.join("<gray>, <#ff6969>", unregisteredPlugins));
        } else {
            return "<#ff6969>No unregistered plugins found";
        }
    }
}
