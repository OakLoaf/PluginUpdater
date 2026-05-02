package org.lushplugins.pluginupdater.api.source;

import org.lushplugins.pluginupdater.api.version.comparator.SemVerComparator;
import org.lushplugins.pluginupdater.api.version.comparator.VersionComparator;

public abstract class SourceData {
    private final String name;
    private final VersionComparator defaultComparator;

    public SourceData(String name, VersionComparator defaultComparator) {
        this.name = name;
        this.defaultComparator = defaultComparator;
    }

    public SourceData(String name) {
        this(name, SemVerComparator.INSTANCE);
    }

    public String getName() {
        return name;
    }

    public VersionComparator getDefaultComparator() {
        return defaultComparator;
    }
}
