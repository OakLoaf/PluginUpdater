package org.lushplugins.pluginupdater.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushlib.command.SubCommand;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.config.ConfigManager;

import java.util.Arrays;
import java.util.List;

public class UnregisteredPluginsSubCommand extends SubCommand {

    public UnregisteredPluginsSubCommand() {
        super("unregisteredplugins");
        addRequiredPermission("pluginupdater.unregisteredplugins");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings, @NotNull String[] strings1) {
        ConfigManager configManager = PluginUpdater.getInstance().getConfigManager();
        List<String> unregisteredPlugins = Arrays.stream(Bukkit.getPluginManager().getPlugins())
            .map(Plugin::getName)
            .filter(pluginName -> configManager.getPluginData(pluginName) == null)
            .sorted()
            .toList();

        if (!unregisteredPlugins.isEmpty()) {
            ChatColorHandler.sendMessage(sender, "&#ff6969Missing plugins:\n" + String.join(", ", unregisteredPlugins));
        } else {
            ChatColorHandler.sendMessage(sender, "&#ff6969No unregistered plugins found");
        }

        return true;
    }
}
