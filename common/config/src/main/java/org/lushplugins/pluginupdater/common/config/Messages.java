package org.lushplugins.pluginupdater.common.config;

import java.util.Map;

public record Messages(Map<String, String> messages) {

    public String get(String key) {
        return messages.get(key);
    }

    public String get(String key, String def) {
        return messages.getOrDefault(key, def);
    }
}
