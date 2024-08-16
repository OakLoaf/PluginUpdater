package org.lushplugins.pluginupdater.command;

import org.lushplugins.lushlib.command.Command;
import org.lushplugins.lushlib.command.SubCommand;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.pluginupdater.PluginUpdater;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public class PluginUpdaterCommand extends Command {

    public PluginUpdaterCommand() {
        super("pluginupdater");
        addSubCommand(new PluginUpdatesCommand());
        addSubCommand(new ReloadSubCommand());
        addSubCommand(new RunChecksSubCommand());
        addSubCommand(new UnregisteredPluginsSubCommand());
        addSubCommand(new UpdateSubCommand());
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
        return true;
    }

    private static class ReloadSubCommand extends SubCommand {

        public ReloadSubCommand() {
            super("reload");
            addRequiredPermission("pluginupdater.reload");
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
            try {
                PluginUpdater.getInstance().getConfigManager().reloadConfig();
            } catch (Throwable e) {
                ChatColorHandler.sendMessage(sender, "&#ff6969Something went wrong whilst reloading the plugin, check the console for errors");
                e.printStackTrace();
                return true;
            }

            ChatColorHandler.sendMessage(sender, "&#b7faa2Successfully reloaded PluginUpdater");
            return true;
        }
    }

    private static class RunChecksSubCommand extends SubCommand {

        public RunChecksSubCommand() {
            super("runchecks");
            addRequiredPermission("pluginupdater.checkupdates");
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
            AtomicInteger updateCount = new AtomicInteger(0);
            PluginUpdater.getInstance().getConfigManager().getPlugins().forEach(pluginName -> {
                PluginUpdater.getInstance().getUpdateHandler().queueUpdateCheck(pluginName);
                updateCount.incrementAndGet();
            });

            ChatColorHandler.sendMessage(sender, "&#b7faa2Successfully queued checks for " + updateCount.get() + " plugins");
            return true;
        }
    }
}
