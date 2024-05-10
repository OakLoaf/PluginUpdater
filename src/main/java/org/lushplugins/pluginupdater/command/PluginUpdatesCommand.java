package org.lushplugins.pluginupdater.command;

import me.dave.chatcolorhandler.ChatColorHandler;
import org.lushplugins.lushlib.command.Command;
import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.config.ConfigManager;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.pluginupdater.updater.VersionDifference;

import java.util.ArrayList;
import java.util.List;

public class PluginUpdatesCommand extends Command {

    public PluginUpdatesCommand() {
        super("updates");
        addRequiredPermission("pluginupdater.checkupdates");
        addSubCommand(new UpdatesListSubCommand());
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
        ConfigManager configManager = PluginUpdater.getInstance().getConfigManager();
        String uncheckedColor = configManager.getMessage("unchecked-color", "&8");
        String updateAvailableColor = configManager.getMessage("update-available-color", "&#ffda54");
        String majorUpdateAvailableColor = configManager.getMessage("major-update-available-color", "&#ff6969");
        String updatePreparedColor = configManager.getMessage("update-prepared-color", "&#ffda54");
        String latestVersionColor = configManager.getMessage("latest-version-color", "&#b7faa2");

        List<String> plugins = new ArrayList<>();
        configManager.getAllPluginData().forEach(pluginData -> {
            String statusFormat;
            VersionDifference versionDifference = pluginData.getVersionDifference();
            if (!pluginData.hasCheckRan()) {
                statusFormat = uncheckedColor + "%plugin%";
            } else if (pluginData.isAlreadyDownloaded()) {
                statusFormat = latestVersionColor + "%plugin%" + updatePreparedColor + "*";
            } else if (versionDifference.equals(VersionDifference.MAJOR)) {
                statusFormat = majorUpdateAvailableColor + "%plugin%";
            } else if (versionDifference.equals(VersionDifference.MINOR) || versionDifference.equals(VersionDifference.BUG_FIXES) || versionDifference.equals(VersionDifference.BUILD)) {
                statusFormat = updateAvailableColor + "%plugin%";
            } else {
                statusFormat = latestVersionColor + "%plugin%";
            }

            plugins.add(statusFormat.replace("%plugin%", pluginData.getPluginName()));
        });

        if (!plugins.isEmpty()) {
            ChatColorHandler.sendMessage(sender, String.join("&7, ", plugins));
        } else {
            ChatColorHandler.sendMessage(sender, "&#ff6969Could not find any registered plugins in PluginUpdater");
        }

        return true;
    }
}
