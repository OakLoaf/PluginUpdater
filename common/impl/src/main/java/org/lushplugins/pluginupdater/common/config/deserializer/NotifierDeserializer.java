package org.lushplugins.pluginupdater.common.config.deserializer;

import com.electronwill.nightconfig.core.Config;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.notifier.DiscordWebHookNotifier;
import org.lushplugins.pluginupdater.common.notifier.Notifier;

public class NotifierDeserializer {

    public static @Nullable Notifier discordWebHookNotifier(UpdaterImpl<?> updater, Config config) {
        String webHookUrl = config.get("webhook-url");
        return !webHookUrl.isBlank() ? new DiscordWebHookNotifier(updater.updaterPlugin().getComponentLogger(), webHookUrl) : null;
    }
}
