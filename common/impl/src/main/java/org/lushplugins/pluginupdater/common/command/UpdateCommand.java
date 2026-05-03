package org.lushplugins.pluginupdater.common.command;

import org.lushplugins.pluginupdater.common.command.annotation.CommandPermission;
import org.lushplugins.pluginupdater.common.platform.UpdaterPlatform;
import org.lushplugins.pluginupdater.common.updater.UpdateHandler;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.version.VersionDifference;
import org.lushplugins.pluginupdater.common.command.annotation.PluginName;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Switch;
import revxrsal.commands.command.CommandActor;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
public record UpdateCommand(UpdaterPlatform instance) {

    @Command("updater update")
    @CommandPermission("pluginupdater.downloadupdates")
    public String update(@PluginName String pluginName) {
        if (!instance.getConfig().shouldAllowDownloads()) {
            return "&#ff6969Update downloads have been disabled in the config";
        }

        PluginData pluginData = instance.getConfig().getPluginData(pluginName);
        if (pluginData == null) {
            return "&#ff6969That plugin is not registered";
        } else if (!pluginData.areDownloadsAllowed()) {
            return "&#ff6969Downloads are disabled for that plugin, to allow downloads manually add it to your config";
        } else if (pluginData.isAlreadyDownloaded()) {
            return "&#ffda54You have already downloaded an update for this plugin - please restart your server";
        } else if (!pluginData.isUpdateAvailable()) {
            return "&#ff6969No update has been found for this plugin";
        } else {
            instance.getUpdateHandler().queueDownload(pluginData.getPluginName());
            return "&#b7faa2Successfully queued an update for '%s'".formatted(pluginData.getPluginName());
        }
    }

    @Command("updater update all")
    @CommandPermission("pluginupdater.downloadupdates")
    public String updateAll(CommandActor actor, @Switch("force") boolean force) {
        UpdateHandler updateHandler = instance.getUpdateHandler();
        AtomicInteger updateCount = new AtomicInteger(0);
        AtomicInteger majorUpdateCount = new AtomicInteger(0);
        instance.getConfig().getAllPluginData().forEach(pluginData -> {
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
            actor.reply("&#ff6969No updates found");
        } else if (finalCount > 0) {
            actor.reply("&#b7faa2Successfully queued an update for %s plugins".formatted(finalCount));
        }

        if (finalMajorCount > 0) {
            actor.reply("&#e0c01b%s &#ffe27aplugins require major updates, run &#e0c01b/updater update all --force &#ffe27ato force all possible updates".formatted(finalMajorCount));
        }

        return null;
    }
}
