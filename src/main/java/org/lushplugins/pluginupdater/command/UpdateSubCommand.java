package org.lushplugins.pluginupdater.command;

import me.dave.chatcolorhandler.ChatColorHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushlib.command.SubCommand;
import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.updater.PluginData;
import org.lushplugins.pluginupdater.updater.UpdateHandler;
import org.lushplugins.pluginupdater.updater.VersionDifference;

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
        if (args[0].equalsIgnoreCase("all")) {
            UpdateHandler updateHandler = PluginUpdater.getInstance().getUpdateHandler();
            AtomicInteger updateCount = new AtomicInteger(0);
            AtomicInteger majorUpdateCount = new AtomicInteger(0);

            PluginUpdater.getInstance().getConfigManager().getAllPluginData().forEach(pluginData -> {
                if (!pluginData.isAlreadyDownloaded() && pluginData.isUpdateAvailable() && !pluginData.getVersionDifference().equals(VersionDifference.MAJOR)) {
                    if (pluginData.getVersionDifference().equals(VersionDifference.MAJOR) && !(args.length == 3 && args[1].equals("-f"))) {
                        majorUpdateCount.incrementAndGet();
                        return;
                    }

                    updateHandler.queueDownload(pluginData.getPluginName());
                    updateCount.incrementAndGet();
                }
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
