package org.lushplugins.pluginupdater.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushlib.command.SubCommand;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.config.ConfigManager;
import org.lushplugins.pluginupdater.api.version.VersionDifference;

import java.util.ArrayList;
import java.util.List;

public class UpdatesListSubCommand extends SubCommand {

    public UpdatesListSubCommand() {
        super("list");
        addRequiredPermission("pluginupdater.checkupdates");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
        ConfigManager configManager = PluginUpdater.getInstance().getConfigManager();
        String updateAvailableColor = configManager.getMessage("update-available-color", "&#ffda54");
        String majorUpdateAvailableColor = configManager.getMessage("major-update-available-color", "&#ff6969");
        String latestVersionColor = configManager.getMessage("latest-version-color", "&#b7faa2");

        List<String> plugins = new ArrayList<>();
        configManager.getAllPluginData().forEach(pluginData -> {
            VersionDifference versionDifference = pluginData.getVersionDifference();
            if (!pluginData.isAlreadyDownloaded() && (versionDifference.equals(VersionDifference.LATEST) || versionDifference.equals(VersionDifference.UNKNOWN))) {
                return;
            }

            String message = "&f" + pluginData.getPluginName() + ": &7" + pluginData.getCurrentVersion() + " &f-> " + (versionDifference.equals(VersionDifference.MAJOR) ? majorUpdateAvailableColor : updateAvailableColor) + pluginData.getLatestVersion();

            if (pluginData.isAlreadyDownloaded()) {
                message += latestVersionColor + " ᴅᴏᴡɴʟᴏᴀᴅᴇᴅ";
            }

            plugins.add(message);
        });

        if (!plugins.isEmpty()) {
            ChatColorHandler.sendMessage(sender, String.join("&r\n", plugins));
        } else {
            ChatColorHandler.sendMessage(sender, "&#ff6969No updates found");
        }

        return true;
    }
}
