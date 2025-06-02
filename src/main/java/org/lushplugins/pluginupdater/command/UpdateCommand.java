package org.lushplugins.pluginupdater.command;

import org.bukkit.command.CommandSender;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.version.VersionDifference;
import org.lushplugins.pluginupdater.updater.UpdateHandler;
import org.lushplugins.pluginupdater.util.lamp.annotation.PluginName;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Switch;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
public class UpdateCommand {

    @Command("updater update")
    @CommandPermission("pluginupdater.downloadupdates")
    public String update(
        CommandSender sender,
        @PluginName String pluginName,
        @Switch("force") boolean force
    ) {
        if (!PluginUpdater.getInstance().getConfigManager().shouldAllowDownloads()) {
            return "&#ff6969Update downloads have been disabled in the config";
        }

        if (pluginName.equalsIgnoreCase("all")) {
            return updateAll(sender, force);
        }

        PluginData pluginData = PluginUpdater.getInstance().getConfigManager().getPluginData(pluginName);
        if (pluginData == null) {
            return "&#ff6969That plugin is not registered";
        } else if (!pluginData.areDownloadsAllowed()) {
            return "&#ff6969Downloads are disabled for that plugin, to allow downloads manually add it to your config";
        } else if (pluginData.isAlreadyDownloaded()) {
            return "&#ffda54You have already downloaded an update for this plugin - please restart your server";
        } else if (!pluginData.isUpdateAvailable()) {
            return "&#ff6969No update has been found for this plugin";
        } else {
            PluginUpdater.getInstance().getUpdateHandler().queueDownload(pluginData.getPluginName());
            return "&#b7faa2Successfully queued an update for '%s'".formatted(pluginData.getPluginName());
        }
    }

    private String updateAll(CommandSender sender, boolean force) {
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

            if (pluginData.getVersionDifference().equals(VersionDifference.MAJOR) && !force) {
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
            ChatColorHandler.sendMessage(sender, "&#b7faa2Successfully queued an update for %s plugins".formatted(finalCount));
        }

        if (finalMajorCount > 0) {
            ChatColorHandler.sendMessage(sender, "&#e0c01b%s &#ffe27aplugins require major updates, run &#e0c01b/updates update all -f &#ffe27ato force all possible updates".formatted(finalMajorCount));
        }

        return null;
    }
}
