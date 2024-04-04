package me.oak.pluginupdater.updater;

import java.io.IOException;

public interface VersionChecker {
    boolean hasUpdate(PluginData pluginData) throws IOException;

    static boolean isLatestVersion(String currentVersionRaw, String newVersionRaw) {
        String[] currVersionParts = currentVersionRaw.split("\\.");
        String[] versionParts = newVersionRaw.split("\\.");

        int i = 0;
        for (String versionPart : versionParts) {
            if (i >= currVersionParts.length) {
                break;
            }

            int newVersion = Integer.parseInt(versionPart);
            int currVersion = Integer.parseInt(currVersionParts[i]);
            if (newVersion > currVersion) {
                if (i != 0) {
                    int newVersionLast = Integer.parseInt(versionParts[i-1]);
                    int currVersionLast = Integer.parseInt(currVersionParts[i-1]);
                    if (newVersionLast >= currVersionLast) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            i++;
        }

        return true;
    }
}
