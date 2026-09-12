package org.lushplugins.pluginupdater.common.notifier;

import io.github._4drian3d.jdwebhooks.component.Component;
import io.github._4drian3d.jdwebhooks.component.ContainerableComponent;
import io.github._4drian3d.jdwebhooks.webhook.WebHookClient;
import io.github._4drian3d.jdwebhooks.webhook.WebHookExecution;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.version.Version;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscordWebhookNotifier {
    private final Logger logger;
    private final WebHookClient webhookClient;
    private final boolean enabled;

    public DiscordWebhookNotifier(Logger logger, boolean enabled, @Nullable String webhookUrl) {
        this.logger = logger;
        this.enabled = enabled && webhookUrl != null && !webhookUrl.isBlank();

        if (this.enabled) {
            this.webhookClient = WebHookClient.fromURL(webhookUrl);
        } else {
            this.webhookClient = null;
        }
    }

    public void notifyDownload(PluginData pluginData) {
        if (!enabled || webhookClient == null) {
            return;
        }

        try {
            Version currentVersion = pluginData.currentVersion();
            Optional<Version> latestVersionOpt = pluginData.latestVersion();

            if (latestVersionOpt.isEmpty()) {
                return;
            }

            Version latestVersion = latestVersionOpt.get();
            String pluginName = pluginData.pluginName();

            String versionString = String.format("%s → %s",
                currentVersion.rawVersionString(),
                latestVersion.rawVersionString()
            );

            List<ContainerableComponent> components = new ArrayList<>();

            components.add(Component.textDisplay("**" + pluginName + " Updated**"));

            components.add(Component.textDisplay("**Version:** " + versionString));

            String versionDiff = pluginData.versionDifference().name();
            if (!versionDiff.equals("UNKNOWN")) {
                components.add(Component.textDisplay("**Update Type:** " + versionDiff));
            }

            pluginData.getChangelogUrl().ifPresent(s -> components.add(Component.textDisplay("**Changelog:** [View Changelog](" + s + ")")));

            WebHookExecution webHook = WebHookExecution.builder()
                .username("PluginUpdater")
                .avatarURL("https://cdn.modrinth.com/data/IBSpJfbm/172c14d2cdb854064160fa627f9dd0043c1b79ee_96.webp")
                .component(
                    Component.container()
                        .components(components)
                        .accentColor(0x66b04f)
                        .build()
                )
                .build();

            webhookClient.executeWebHook(webHook)
                .whenComplete((response, throwable) -> {
                    int statusCode = response.statusCode();

                    if (throwable != null) {
                        logger.log(Level.WARNING, "Failed to send Discord webhook notification", throwable);
                    } else if (statusCode < 200 || statusCode >= 300) {
                        logger.log(
                            Level.WARNING,
                            () -> "Discord webhook returned unsuccessful response. Status code: "
                                + statusCode
                                + ", response: "
                                + response
                        );
                    }
                });

        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to send Discord webhook notification", e);
        }
    }

    public void close() {
        // Might come handy in the future
    }
}
