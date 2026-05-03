package org.lushplugins.pluginupdater.paper.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.lushplugins.pluginupdater.api.updater.Updater;
import org.lushplugins.pluginupdater.paper.api.notification.PaperUpdateNotifier;
import org.lushplugins.pluginupdater.paper.api.plugin.PaperPluginInfo;

public class PaperUpdater {

    public static Updater.Builder builder(Plugin plugin) {
        return Updater.builder(new PaperPluginInfo(plugin), Bukkit.getUpdateFolderFile())
            .notifier(PaperUpdateNotifier::new);
    }
}
