package org.lushplugins.pluginupdater.api.platform;

public abstract class PlatformData {
    private final String name;

    public PlatformData(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
