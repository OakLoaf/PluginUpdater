package org.lushplugins.pluginupdater.api.platform;

import org.lushplugins.pluginupdater.api.version.comparator.SemVerComparator;
import org.lushplugins.pluginupdater.api.version.comparator.VersionComparator;

public abstract class PlatformData {
    private final String name;
    private final VersionComparator defaultComparator;

    public PlatformData(String name, VersionComparator defaultComparator) {
        this.name = name;
        this.defaultComparator = defaultComparator;
    }

    public PlatformData(String name) {
        this(name, SemVerComparator.INSTANCE);
    }

    public String getName() {
        return name;
    }

    public VersionComparator getDefaultComparator() {
        return defaultComparator;
    }
}
