package me.oak.pluginupdater.command;

import me.dave.chatcolorhandler.ChatColorHandler;
import me.oak.pluginupdater.PluginUpdater;
import me.oak.pluginupdater.config.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PluginUpdatesCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        ConfigManager configManager = PluginUpdater.getInstance().getConfigManager();
        if (!sender.hasPermission("pluginupdater.checkupdates")) {
            ChatColorHandler.sendMessage(sender, configManager.getMessage("insufficient-permissions", "&#ff6969You don't have the sufficient permissions for this command"));
            return true;
        }

        String uncheckedColor = configManager.getMessage("unchecked-color", "&8");
        String outdatedColor = configManager.getMessage("outdated-color", "&#ff6969");
        String updatePreparedColor = configManager.getMessage("update-prepared-color", "&#ffda54");
        String latestVersionColor = configManager.getMessage("latest-version-color", "&#b7faa2");

        List<String> plugins = new ArrayList<>();
        configManager.getAllPluginData().forEach(pluginData -> {
            String statusColor;
            if (!pluginData.hasCheckRan()) {
                statusColor = uncheckedColor;
            } else if (pluginData.isAlreadyDownloaded()) {
                statusColor = updatePreparedColor;
            } else if (pluginData.isUpdateAvailable()) {
                statusColor = outdatedColor;
            } else {
                statusColor = latestVersionColor;
            }

            plugins.add(statusColor + pluginData.getPluginName());
        });

        if (!plugins.isEmpty()) {
            ChatColorHandler.sendMessage(sender, String.join("&7, ", plugins));
        } else {
            ChatColorHandler.sendMessage(sender, "&#ff6969Could not find any registered plugins in PluginUpdater");
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
