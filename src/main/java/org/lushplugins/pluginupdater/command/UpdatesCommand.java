package org.lushplugins.pluginupdater.command;

import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.config.ConfigManager;
import org.lushplugins.pluginupdater.api.version.VersionDifference;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class UpdatesCommand {

    @Command({ "updater updates", "updates" })
    @CommandPermission("pluginupdater.checkupdates")
    public String updates() {
        ConfigManager configManager = PluginUpdater.getInstance().getConfigManager();
        List<String> plugins = configManager.getAllPluginData().stream()
            .map(pluginData -> {
                String pluginName = pluginData.getPluginName();
                VersionDifference versionDifference = pluginData.getVersionDifference();
                if (!pluginData.hasCheckRan()) {
                    return configManager.getMessage("unchecked-color", "&8")
                        + pluginName;
                } else if (pluginData.isAlreadyDownloaded()) {
                    return configManager.getMessage("latest-version-color", "&#b7faa2")
                        + pluginName
                        + configManager.getMessage("update-prepared-color", "&#ffda54")
                        + "*";
                } else if (versionDifference.equals(VersionDifference.MAJOR)) {
                    return configManager.getMessage("major-update-available-color", "&#ff6969")
                        + pluginName;
                } else if (versionDifference.equals(VersionDifference.MINOR) || versionDifference.equals(VersionDifference.BUG_FIXES) || versionDifference.equals(VersionDifference.BUILD)) {
                    return configManager.getMessage("update-available-color", "&#ffda54")
                        + pluginName;
                } else {
                    return configManager.getMessage("latest-version-color", "&#b7faa2")
                        + pluginName;
                }
            })
            .toList();

        if (!plugins.isEmpty()) {
            return String.join("&7, ", plugins);
        } else {
            return "&#ff6969Could not find any registered plugins in PluginUpdater";
        }
    }

    @Command({ "updater updates list", "updates list" })
    @CommandPermission("pluginupdater.checkupdates")
    public String list() {
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
            return String.join("&r\n", plugins);
        } else {
            return "&#ff6969No updates found";
        }
    }
}
