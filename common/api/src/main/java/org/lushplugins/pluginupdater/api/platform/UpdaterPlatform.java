package org.lushplugins.pluginupdater.api.platform;

import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;

import java.util.Collection;
import java.util.List;

public interface UpdaterPlatform<T> {

    @Nullable PluginInfo getPlugin(String name);

    List<? extends PluginInfo> getPlugins();

    Collection<T> getOnlineUsers();

    default List<T> getOnlineUsersWithPermission(@Nullable String permission) {
        return getOnlineUsers().stream()
            .filter(user -> hasPermission(user, permission))
            .toList();
    }

    boolean hasPermission(T user, String permission);

    void sendMessage(T user, String message);

    void broadcastMessage(Collection<T> users, String message);

    void sendActionBar(T user, String message);

    void broadcastActionBar(List<T> users, String message);
}
