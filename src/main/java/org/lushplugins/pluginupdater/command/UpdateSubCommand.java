package org.lushplugins.pluginupdater.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushlib.command.SubCommand;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.updater.UpdateHandler;
import org.lushplugins.pluginupdater.api.version.VersionDifference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class UpdateSubCommand extends SubCommand {

    public UpdateSubCommand() {
        super("update");
        addRequiredPermission("pluginupdater.downloadupdates");
        addRequiredArgs(0, () -> {
            List<String> plugins = new ArrayList<>();
            plugins.add("all");
            plugins.addAll(PluginUpdater.getInstance().getConfigManager().getPlugins());
            return plugins;
        });
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
        if (!PluginUpdater.getInstance().getConfigManager().shouldAllowDownloads()) {
            ChatColorHandler.sendMessage(sender, "&#ff6969Update downloads have been disabled in the config");
            return true;
        }

        if (args.length == 0) {
            ChatColorHandler.sendMessage(sender, "&#ff6969Invalid command format, try: /updater update <plugin>");
            return true;
        }

        if (args[0].equalsIgnoreCase("all")) {
            UpdateHandler updateHandler = PluginUpdater.getInstance().getUpdateHandler();
            AtomicInteger updateCount = new AtomicInteger(0);
            AtomicInteger majorUpdateCount = new AtomicInteger(0);

            PluginUpdater.getInstance().getConfigManager().getAllPluginData().forEach(pluginData -> {
                if (!pluginData.areDownloadsAllowed()) {
                    return;
                }

                if (pluginData.isAlreadyDownloaded() || !pluginData.isUpdateAvailable()) {
                    return;
                }

                if (pluginData.getVersionDifference().equals(VersionDifference.MAJOR) && !(args.length == 3 && args[1].equals("-f"))) {
                    majorUpdateCount.incrementAndGet();
                    return;
                }

                updateHandler.queueDownload(pluginData.getPluginName());
                updateCount.incrementAndGet();
            });

            int finalCount = updateCount.get();
            int finalMajorCount = majorUpdateCount.get();

            if (finalCount == 0 && finalMajorCount == 0) {
                ChatColorHandler.sendMessage(sender, "&#ff6969No updates found");
            } else if (finalCount > 0) {
                ChatColorHandler.sendMessage(sender, "&#b7faa2Successfully queued an update for " + finalCount + " plugins");
            }

            if (finalMajorCount > 0) {
                ChatColorHandler.sendMessage(sender, "&#e0c01b" + finalMajorCount + " &#ffe27aplugins require major updates, run &#e0c01b/updates update all -f &#ffe27ato force all possible updates");
            }

            return true;
        }

        PluginData pluginData = PluginUpdater.getInstance().getConfigManager().getPluginData(args[0]);
        if (pluginData == null) {
            ChatColorHandler.sendMessage(sender, "&#ff6969That plugin is not registered");
        }
        else if (!pluginData.areDownloadsAllowed()) {
            ChatColorHandler.sendMessage(sender, "&#ff6969Downloads are disabled for that plugin, to allow downloads manually add it to your config");
        }
        else if (pluginData.isAlreadyDownloaded()) {
            ChatColorHandler.sendMessage(sender, "&#ffda54You have already downloaded an update for this plugin - please restart your server");
        }
        else if (!pluginData.isUpdateAvailable()) {
            ChatColorHandler.sendMessage(sender, "&#ff6969No update has been found for this plugin");
        }
        else {
            PluginUpdater.getInstance().getUpdateHandler().queueDownload(pluginData.getPluginName());
            ChatColorHandler.sendMessage(sender, "&#b7faa2Successfully queued an update for '" + pluginData.getPluginName() + "'");
        }

        return true;
    }
}
