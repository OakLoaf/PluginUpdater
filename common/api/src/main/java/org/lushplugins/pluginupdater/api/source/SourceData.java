package org.lushplugins.pluginupdater.api.source;

import org.lushplugins.pluginupdater.api.version.comparator.SemVerComparator;
import org.lushplugins.pluginupdater.api.version.comparator.VersionComparator;

public interface SourceData {

    String name();

    default VersionComparator getDefaultComparator() {
        return SemVerComparator.INSTANCE;
    }
}
