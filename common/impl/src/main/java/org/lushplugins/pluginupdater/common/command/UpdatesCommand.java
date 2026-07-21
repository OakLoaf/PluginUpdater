package org.lushplugins.pluginupdater.common.command;

import org.lushplugins.pluginupdater.api.version.Version;
import org.lushplugins.pluginupdater.common.command.annotation.CommandPermission;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.api.version.VersionDifference;
import org.lushplugins.pluginupdater.common.config.ConfigManager;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.orphan.OrphanCommand;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public record UpdatesCommand(UpdaterImpl<?> updater) implements OrphanCommand {

    @CommandPlaceholder
    @CommandPermission("pluginupdater.checkupdates")
    public static String updates(UpdaterImpl<?> updater) {
        ConfigManager configManager = updater.config();
        List<String> plugins = configManager.getAllPluginData().stream()
            .map(pluginData -> {
                String pluginName = pluginData.pluginName();
                VersionDifference versionDifference = pluginData.versionDifference();
                if (!pluginData.hasCheckRan()) {
                    return configManager.getMessage("unchecked-color", "<gray>")
                        + pluginName;
                } else if (pluginData.isAlreadyDownloaded()) {
                    return configManager.getMessage("latest-version-color", "<#b7faa2>")
                        + pluginName
                        + configManager.getMessage("update-prepared-color", "<#ffda54>")
                        + "*";
                } else if (versionDifference.equals(VersionDifference.MAJOR)) {
                    String name = configManager.getMessage("major-update-available-color", "<#ff6969>")
                        + pluginName;

                    if (pluginData.latestVersion().map(Version::potentiallyUnsafe).orElse(false)) {
                        name += "<hover:show_text:This version is marked as potentially unsafe for your server version>"
                            + configManager.getMessage("major-update-available-color", "<#ff6969>")
                            + "*"
                            + "</hover>";
                    }

                    return name;
                } else if (versionDifference.equals(VersionDifference.MINOR) || versionDifference.equals(VersionDifference.PATCH) || versionDifference.equals(VersionDifference.BUILD)) {
                    String name = configManager.getMessage("update-available-color", "<#ffda54>")
                        + pluginName;

                    if (pluginData.latestVersion().map(Version::potentiallyUnsafe).orElse(false)) {
                        name += "<hover:show_text:This version is marked as potentially unsafe for your server version>"
                            + configManager.getMessage("major-update-available-color", "<#ff6969>")
                            + "*"
                            + "</hover>";
                    }

                    return name;
                } else {
                    return configManager.getMessage("latest-version-color", "<#b7faa2>")
                        + pluginName;
                }
            })
            .toList();

        if (!plugins.isEmpty()) {
            return "<white>Registered Plugins (%s):\n%s".formatted(plugins.size(), String.join("<gray>, ", plugins));
        } else {
            return "<#ff6969>Could not find any registered plugins in PluginUpdater";
        }
    }

    @Subcommand("list")
    @CommandPermission("pluginupdater.checkupdates")
    public static String listUpdates(UpdaterImpl<?> updater) {
        ConfigManager configManager = updater.config();
        String updateAvailableColor = configManager.getMessage("update-available-color", "<#ffda54>");
        String majorUpdateAvailableColor = configManager.getMessage("major-update-available-color", "<#ff6969>");
        String latestVersionColor = configManager.getMessage("latest-version-color", "<#b7faa2>");

        List<String> plugins = new ArrayList<>();
        configManager.getAllPluginData().forEach(pluginData -> {
            VersionDifference versionDifference = pluginData.versionDifference();
            if (!pluginData.isAlreadyDownloaded() && (versionDifference.equals(VersionDifference.LATEST) || versionDifference.equals(VersionDifference.UNKNOWN))) {
                return;
            }

            String interactComponent = pluginData.getChangelogUrl()
                .map((changelogUrl) -> "<hover:show_text:Open %s changelog><click:open_url:%s>"
                    .formatted(pluginData.pluginName(), changelogUrl))
                .orElse("");

            Version latestVersion = pluginData.latestVersion().orElseThrow();
            String message = "%s<white>%s: <gray>%s <white>-> %s%s"
                .formatted(
                    interactComponent,
                    pluginData.pluginName(),
                    pluginData.currentVersion().rawVersionString(),
                    versionDifference.equals(VersionDifference.MAJOR) ? majorUpdateAvailableColor : updateAvailableColor,
                    pluginData.latestVersion().orElseThrow().rawVersionString()
                );

            if (latestVersion.potentiallyUnsafe()) {
                message += "<hover:show_text:This version is marked as potentially unsafe for your server version>"
                    + majorUpdateAvailableColor
                    + "*"
                    + "</hover>";
            }

            if (pluginData.isAlreadyDownloaded()) {
                message += latestVersionColor + " ᴅᴏᴡɴʟᴏᴀᴅᴇᴅ";
            }

            plugins.add(message);
        });

        if (!plugins.isEmpty()) {
            return String.join("<reset>\n", plugins);
        } else {
            return "<#ff6969>No updates found";
        }
    }
}
