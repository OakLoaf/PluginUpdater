package me.oak.pluginupdater.command;

import me.dave.chatcolorhandler.ChatColorHandler;
import me.oak.pluginupdater.PluginUpdater;
import me.oak.pluginupdater.updater.PluginData;
import me.oak.pluginupdater.updater.UpdateHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PluginUpdaterCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (args.length) {
            case 1 -> {
                switch (args[0].toLowerCase()) {
                    case "update" -> {
                        ChatColorHandler.sendMessage(sender, "&#ff6969Incorrect formatting, try /updater update <plugin>");
                        return true;
                    }
                    case "reload" -> {
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
                    case "runchecks" -> {
                        AtomicInteger updateCount = new AtomicInteger(0);
                        PluginUpdater.getInstance().getConfigManager().getPlugins().forEach(pluginName -> {
                            PluginUpdater.getInstance().getUpdateHandler().queueUpdateCheck(pluginName);
                            updateCount.incrementAndGet();
                        });

                        ChatColorHandler.sendMessage(sender, "&#b7faa2Successfully queued checks for " + updateCount.get() + " plugins");
                    }
                }
            }
            case 2 -> {
                if (args[0].equalsIgnoreCase("update")) {
                    // TODO: Add warnings to major version changes, add confirm command, show version changes list before asking confirmation
                    if (sender.hasPermission("pluginupdater.downloadupdates")) {
                        if (args[1].equalsIgnoreCase("all")) {
                            UpdateHandler updateHandler = PluginUpdater.getInstance().getUpdateHandler();
                            AtomicInteger updateCount = new AtomicInteger(0);
                            PluginUpdater.getInstance().getConfigManager().getAllPluginData().forEach(pluginData -> {
                                if (!pluginData.isAlreadyDownloaded() && pluginData.isUpdateAvailable()) {
                                    updateHandler.queueDownload(pluginData.getPluginName());
                                    updateCount.incrementAndGet();
                                }
                            });

                            int finalCount = updateCount.get();
                            if (finalCount == 0) {
                                ChatColorHandler.sendMessage(sender, "&#ff6969No updates found");
                            } else {
                                ChatColorHandler.sendMessage(sender, "&#b7faa2Successfully queued an update for " + finalCount + " plugins");
                            }

                            return true;
                        }

                        PluginData pluginData = PluginUpdater.getInstance().getConfigManager().getPluginData(args[1]);
                        if (pluginData == null) {
                            ChatColorHandler.sendMessage(sender, "&#ff6969That plugin is not registered");
                        }
                        else if (pluginData.isAlreadyDownloaded()) {
                            ChatColorHandler.sendMessage(sender, "&#ffda54You have already downloaded an update for this plugin - please restart your server");
                        }
                        else if (!pluginData.isUpdateAvailable()) {
                            ChatColorHandler.sendMessage(sender, "&#ff6969No update has been found for this plugin");
                        }
                        else {
                            PluginUpdater.getInstance().getUpdateHandler().queueDownload(pluginData.getPluginName());
                            ChatColorHandler.sendMessage(sender, "&#b7faa2Successfully queued an update for '" + pluginData.getPluginName() + "'");
                        }

                        return true;
                    }
                }
            }
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> tabComplete = new ArrayList<>();
        List<String> wordCompletion = new ArrayList<>();
        boolean wordCompletionSuccess = false;

        switch (args.length) {
            case 1 -> {
                if (sender.hasPermission("pluginupdater.downloadupdates")) {
                    tabComplete.add("update");
                }
                if (sender.hasPermission("pluginupdater.checkupdates")) {
                    tabComplete.add("runchecks");
                }
                if (sender.hasPermission("pluginupdater.reload")) {
                    tabComplete.add("reload");
                }
            }
            case 2 -> {
                if (args[0].equalsIgnoreCase("update")) {
                    if (sender.hasPermission("pluginupdater.downloadupdates")) {
                        tabComplete.add("all");
                        tabComplete.addAll(PluginUpdater.getInstance().getConfigManager().getPlugins());
                    }
                }
            }
        }

        for (String currTab : tabComplete) {
            int currArg = args.length - 1;
            if (currTab.startsWith(args[currArg])) {
                wordCompletion.add(currTab);
                wordCompletionSuccess = true;
            }
        }

        return wordCompletionSuccess ? wordCompletion : tabComplete;
    }
}
