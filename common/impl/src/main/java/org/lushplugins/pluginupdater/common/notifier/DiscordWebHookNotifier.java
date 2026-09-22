package org.lushplugins.pluginupdater.common.notifier;

import io.github._4drian3d.jdwebhooks.component.Component;
import io.github._4drian3d.jdwebhooks.component.ContainerableComponent;
import io.github._4drian3d.jdwebhooks.webhook.WebHookClient;
import io.github._4drian3d.jdwebhooks.webhook.WebHookExecution;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.version.Version;
import org.lushplugins.pluginupdater.api.version.VersionDifference;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DiscordWebHookNotifier {
    private final ComponentLogger logger;
    private final WebHookClient webHookClient;

    public DiscordWebHookNotifier(ComponentLogger logger, @NotNull String webhookUrl) {
        this.logger = logger;

        try {
            this.webHookClient = WebHookClient.fromURL(webhookUrl);
        } catch (Exception e) {
            logger.warn("Failed to create Discord webhook client. Webhook notifications will be disabled. Possible wrong url.", e);
            throw e;
        }
    }

    public void notifyDownload(PluginData pluginData) {
        try {
            Version currentVersion = pluginData.currentVersion();
            Optional<Version> latestVersionOptional = pluginData.latestVersion();
            if (latestVersionOptional.isEmpty()) {
                return;
            }

            String pluginName = pluginData.pluginName();

            String versionString = String.format("%s → %s",
                currentVersion.rawVersionString(),
                latestVersionOptional.get().rawVersionString()
            );

            List<ContainerableComponent> components = new ArrayList<>();
            components.add(Component.textDisplay("**" + pluginName + " Updated**"));
            components.add(Component.textDisplay("**Version:** " + versionString));

            VersionDifference versionDiff = pluginData.versionDifference();
            if (versionDiff != VersionDifference.UNKNOWN) {
                components.add(Component.textDisplay("**Update Type:** " + versionDiff.name()));
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

            webHookClient.executeWebHook(webHook)
                .whenComplete((response, throwable) -> {
                    int statusCode = response.statusCode();

                    if (throwable != null) {
                        logger.warn("Failed to send Discord webhook notification", throwable);
                    } else if (statusCode < 200 || statusCode >= 300) {
                        logger.warn("Discord webhook returned unsuccessful response. Status code: {}, response: {}", statusCode, response);
                    }
                });

        } catch (Exception e) {
            logger.warn("Failed to send Discord webhook notification", e);
        }
    }

    public void shutdown() {
        // Might come handy in the future
    }
}
