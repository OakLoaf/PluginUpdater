package org.lushplugins.pluginupdater.paper.test;

import org.junit.jupiter.api.Test;
import org.lushplugins.pluginupdater.tests.commonplugins.CommonPluginsTest;

public class VelocityUpdaterTests {

    static {
        System.setProperty("platform", "velocity");
        System.setProperty("server-version", "4.1.0");
    }

    @Test
    public void test() {
        CommonPluginsTest.runTest("common-plugins.yml");
    }
}
