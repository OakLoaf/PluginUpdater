package org.lushplugins.pluginupdater.geyser.api.util;

import org.cloudburstmc.protocol.bedrock.packet.SetTitlePacket;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.session.GeyserSession;

import java.nio.file.Path;

public class GeyserUtil {
    private static final Path UPDATE_FOLDER = GeyserApi.api().configDirectory()
        .getParent()
        .resolve("extensions")
        .resolve("update");

    public static Path getUpdateFolder() {
        return UPDATE_FOLDER;
    }

    public static void sendActionBar(GeyserSession session, String text) {
        SetTitlePacket titlePacket = new SetTitlePacket();
        titlePacket.setType(SetTitlePacket.Type.ACTIONBAR);
        titlePacket.setText(text);
        titlePacket.setXuid("");
        titlePacket.setPlatformOnlineId("");
        session.sendUpstreamPacket(titlePacket);
    }
}
