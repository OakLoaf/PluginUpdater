package org.lushplugins.pluginupdater.paper.test;

import org.junit.jupiter.api.Test;
import org.lushplugins.pluginupdater.tests.commonplugins.CommonPluginsTest;

public class PaperUpdaterTests {

    static {
        System.setProperty("platform", "paper");
        System.setProperty("server-version", "26.1.2");
    }

    @Test
    public void test() {
        CommonPluginsTest.runTest("common-plugins.yml");
    }
}
