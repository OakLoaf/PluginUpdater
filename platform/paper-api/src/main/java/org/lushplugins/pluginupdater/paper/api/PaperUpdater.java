package org.lushplugins.pluginupdater.paper.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.lushplugins.pluginupdater.api.updater.Updater;
import org.lushplugins.pluginupdater.paper.api.listener.PlayerListener;
import org.lushplugins.pluginupdater.paper.api.platform.PaperUpdaterPlatform;
import org.lushplugins.pluginupdater.paper.api.plugin.PaperPluginInfo;

public class PaperUpdater {

    public static Updater.Builder<Player> builder(Plugin plugin) {
        return Updater.builder(new PaperUpdaterPlatform(), new PaperPluginInfo(plugin))
            .downloadDir(Bukkit.getUpdateFolderFile().toPath())
            .onBuild(PlayerListener::new);
    }
}
