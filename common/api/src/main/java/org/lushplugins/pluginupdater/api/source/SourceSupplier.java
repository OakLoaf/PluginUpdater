package org.lushplugins.pluginupdater.api.source;

import java.io.IOException;

@FunctionalInterface
public interface SourceSupplier<T> {
    T apply(SourceContext context) throws IOException, InterruptedException;
}
