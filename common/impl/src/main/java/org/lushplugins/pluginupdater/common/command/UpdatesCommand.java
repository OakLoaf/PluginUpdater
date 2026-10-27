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
import java.util.Collections;
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
                    String uncheckedColor = configManager.getMessage("unchecked-color", "<dark_gray>");
                    return uncheckedColor + pluginName + closeRawComponent(uncheckedColor);
                } else if (pluginData.isAlreadyDownloaded()) {
                    String latestVersionColor = configManager.getMessage("latest-version-color", "<#b7faa2>");
                    String updatePreparedColor = configManager.getMessage("update-prepared-color", "<#ffda54>");
                    return latestVersionColor + pluginName + closeRawComponent(latestVersionColor)
                        + updatePreparedColor + "*" + closeRawComponent(updatePreparedColor);
                } else if (versionDifference.equals(VersionDifference.MAJOR)) {
                    String majorUpdateAvailableColor = configManager.getMessage("major-update-available-color", "<#ff6969>");
                    String name = majorUpdateAvailableColor + pluginName + closeRawComponent(majorUpdateAvailableColor);

                    if (pluginData.latestVersion().map(Version::hasWarningTags).orElse(false)) {
                        name += "<hover:show_text:This version is marked as potentially unsafe for your server version>"
                            + majorUpdateAvailableColor + "*" + closeRawComponent(majorUpdateAvailableColor)
                            + "</hover>";
                    }

                    return name;
                } else if (versionDifference.equals(VersionDifference.MINOR) || versionDifference.equals(VersionDifference.PATCH) || versionDifference.equals(VersionDifference.BUILD)) {
                    String updateAvailableColor = configManager.getMessage("update-available-color", "<#ffda54>");
                    String name = updateAvailableColor + pluginName + closeRawComponent(updateAvailableColor);

                    List<String> warningTags = pluginData.latestVersion()
                        .map(Version::warningTags)
                        .orElse(Collections.emptyList());
                    if (!warningTags.isEmpty()) {
                        String majorUpdateAvailableColor = configManager.getMessage("major-update-available-color", "<#ff6969>");
                        String warningTagsStr = String.join("\n", warningTags);
                        name += "<hover:show_text:" + warningTagsStr + ">"
                            + majorUpdateAvailableColor + "*" + closeRawComponent(majorUpdateAvailableColor)
                            + "</hover>";
                    }

                    return name;
                } else {
                    String latestVersionColor = configManager.getMessage("latest-version-color", "<#b7faa2>");
                    return latestVersionColor + pluginName + closeRawComponent(latestVersionColor);
                }
            })
            .toList();

        if (!plugins.isEmpty()) {
            return "<white>Registered Plugins (%s):</white>\n%s".formatted(plugins.size(), String.join("<gray>,</gray> ", plugins));
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
            if (!pluginData.isAlreadyDownloaded() && (versionDifference.isLatest() || versionDifference.equals(VersionDifference.UNKNOWN))) {
                return;
            }

            String interactComponent = pluginData.getChangelogUrl()
                .map((changelogUrl) -> "<hover:show_text:Open %s changelog><click:open_url:%s>"
                    .formatted(pluginData.pluginName(), changelogUrl))
                .orElse("");

            Version latestVersion = pluginData.latestVersion().orElseThrow();
            String updateColor = versionDifference.equals(VersionDifference.MAJOR) ? majorUpdateAvailableColor : updateAvailableColor;
            String message = "%s<white>%s: <gray>%s</gray> -> %s%s%s%s</white>"
                .formatted(
                    interactComponent,
                    pluginData.pluginName(),
                    pluginData.currentVersion().rawVersionString(),
                    updateColor,
                    pluginData.latestVersion().orElseThrow().rawVersionString(),
                    closeRawComponent(updateColor),
                    !interactComponent.isBlank() ? "</hover>" : ""
                );

            if (latestVersion.hasWarningTags()) {
                String warningTags = String.join("\n", latestVersion.warningTags());
                message += "<hover:show_text:" + warningTags + ">"
                    + majorUpdateAvailableColor + "*" + closeRawComponent(majorUpdateAvailableColor)
                    + "</hover>";
            }

            if (pluginData.isAlreadyDownloaded()) {
                message += latestVersionColor + " ᴅᴏᴡɴʟᴏᴀᴅᴇᴅ" + closeRawComponent(latestVersionColor);
            }

            plugins.add(message);
        });

        if (!plugins.isEmpty()) {
            return String.join("<reset>\n", plugins);
        } else {
            return "<#ff6969>No updates found";
        }
    }

    private static String closeRawComponent(String rawComponent) {
        return rawComponent.replace("<", "</");
    }
}
