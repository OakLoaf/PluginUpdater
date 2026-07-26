package org.lushplugins.pluginupdater.paper.api;

import io.papermc.paper.ServerBuildInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.lushplugins.pluginupdater.api.source.SourceRegistry;
import org.lushplugins.pluginupdater.api.source.type.GeyserSource;
import org.lushplugins.pluginupdater.api.source.type.HangarSource;
import org.lushplugins.pluginupdater.api.source.type.ModrinthSource;
import org.lushplugins.pluginupdater.api.updater.Updater;
import org.lushplugins.pluginupdater.paper.api.listener.PlayerListener;
import org.lushplugins.pluginupdater.paper.api.platform.PaperUpdaterPlatform;
import org.lushplugins.pluginupdater.paper.api.plugin.PaperPluginInfo;
import org.lushplugins.pluginupdater.paper.api.util.PaperUtil;

import java.util.List;

public class PaperUpdater {
    private static boolean registriesPopulated = false;

    public static void populateRegistries() {
        if (registriesPopulated) {
            return;
        }
        registriesPopulated = true;

        String serverVersion = ServerBuildInfo.buildInfo().minecraftVersionId();

        SourceRegistry.register(new GeyserSource("spigot"));
        SourceRegistry.register(new HangarSource("paper", serverVersion));
        SourceRegistry.register(new ModrinthSource(
            PaperUtil.isFolia() ? List.of("folia") : List.of("bukkit", "spigot", "paper", "purpur"),
            serverVersion));
    }

    public static Updater.Builder<Player> builder(Plugin plugin) {
        populateRegistries();

        return Updater.builder(new PaperUpdaterPlatform(), new PaperPluginInfo(plugin))
            .downloadDir(Bukkit.getUpdateFolderFile().toPath())
            .onBuild(PlayerListener::new);
    }
}
