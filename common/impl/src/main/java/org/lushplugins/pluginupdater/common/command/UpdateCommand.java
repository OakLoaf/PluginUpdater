package org.lushplugins.pluginupdater.common.command;

import org.lushplugins.pluginupdater.api.version.Version;
import org.lushplugins.pluginupdater.common.command.annotation.CommandPermission;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.updater.UpdateHandler;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.version.VersionDifference;
import org.lushplugins.pluginupdater.common.command.annotation.PluginName;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.annotation.Switch;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.orphan.OrphanCommand;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public record UpdateCommand(UpdaterImpl<?> updater) implements OrphanCommand {

    @Subcommand("update")
    @CommandPermission("pluginupdater.downloadupdates")
    public String update(CommandActor actor, @PluginName(includeTags = true, withUpdateAvailable = true) String pluginName) {
        if (!updater.config().shouldAllowDownloads()) {
            return "<#ff6969>Update downloads have been disabled in the config";
        }

        if (pluginName.startsWith("#")) {
            String tag = pluginName.substring(1);
            updateAll(actor, (pluginData) -> pluginData.hasTag(tag), true, true);
            return null;
        } else if (pluginName.startsWith("$")) {
            String sourceName = pluginName.substring(1);
            updateAll(actor, (pluginData) -> pluginData.sourceData().stream().anyMatch(source -> {
                return source.sourceName().equals(sourceName);
            }), true, true);
            return null;
        }

        PluginData pluginData = updater.config().getPluginData(pluginName);
        if (pluginData == null) {
            return "<#ff6969>That plugin is not registered";
        } else if (!pluginData.areDownloadsAllowed()) {
            return "<#ff6969>Downloads are disabled for that plugin, to allow downloads manually add it to your config";
        } else if (pluginData.isAlreadyDownloaded()) {
            return "<#ffda54>You have already downloaded an update for this plugin - please restart your server";
        } else if (!pluginData.isUpdateAvailable()) {
            return "<#ff6969>No update has been found for this plugin";
        } else {
            updater.updateHandler().queueDownload(pluginData.pluginName());
            return "<#b7faa2>Successfully queued an update for '%s'".formatted(pluginData.pluginName());
        }
    }

    @Subcommand("update all")
    @CommandPermission("pluginupdater.downloadupdates")
    public void updateAll(
        CommandActor actor,
        @Switch(value = "ignore-warnings", shorthand = 'w') boolean ignoreWarnings,
        @Switch(value = "include-major", shorthand = 'f') boolean includeMajorChanges
    ) {
        updateAll(actor, (ignored) -> true, ignoreWarnings, includeMajorChanges);
    }

    public void updateAll(CommandActor actor, Predicate<PluginData> predicate, boolean ignoreWarnings, boolean includeMajorChanges) {
        UpdateHandler<?> updateHandler = updater.updateHandler();
        AtomicInteger updateCount = new AtomicInteger(0);
        AtomicInteger warningTagCount = new AtomicInteger(0);
        AtomicInteger majorUpdateCount = new AtomicInteger(0);
        updater.config().getAllPluginData().stream()
            .filter(predicate)
            .forEach(pluginData -> {
                if (!pluginData.areDownloadsAllowed()) {
                    return;
                }

                if (pluginData.isAlreadyDownloaded() || !pluginData.isUpdateAvailable()) {
                    return;
                }

                if (!ignoreWarnings && pluginData.latestVersion().map(Version::hasWarningTags).orElse(false)) {
                    warningTagCount.incrementAndGet();
                    return;
                }

                if (!includeMajorChanges && pluginData.versionDifference().equals(VersionDifference.MAJOR)) {
                    majorUpdateCount.incrementAndGet();
                    return;
                }

                updateHandler.queueDownload(pluginData.pluginName());
                updateCount.incrementAndGet();
            });

        int finalCount = updateCount.get();
        int finalWarningTagCount = warningTagCount.get();
        int finalMajorCount = majorUpdateCount.get();

        if (finalCount == 0 && finalWarningTagCount == 0 && finalMajorCount == 0) {
            actor.reply("<#ff6969>No updates found");
        } else if (finalCount > 0) {
            actor.reply("<#b7faa2>Successfully queued an update for %s plugins".formatted(finalCount));
        }

        if (finalWarningTagCount > 0) {
            actor.reply("<#e0c01b>%s <#ffe27a>plugins are marked as unsafe, use the <#e0c01b>--ignore-warnings <#ffe27a>flag to ignore warning tags".formatted(finalWarningTagCount));
        }

        if (finalMajorCount > 0) {
            actor.reply("<#e0c01b>%s <#ffe27a>plugins require major updates, use the <#e0c01b>--include-major <#ffe27a>flag to include major updates".formatted(finalMajorCount));
        }
    }
}
