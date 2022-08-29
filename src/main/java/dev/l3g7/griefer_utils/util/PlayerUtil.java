package dev.l3g7.griefer_utils.util;

import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.IChatComponent;

import java.util.UUID;

import static dev.l3g7.griefer_utils.features.Feature.mc;

public class PlayerUtil {

    public static String unnick(String nickedName) {
        for(NetworkPlayerInfo info : mc().getNetHandler().getPlayerInfoMap()) {
            if(info.getDisplayName() != null) {
                String[] parts = info.getDisplayName().getUnformattedText().split("\u2503");
                if(parts.length > 1 && parts[1].trim().equals(nickedName)) {
                    return info.getGameProfile().getName();
                }
            }
        }
        return nickedName;
    }

    public static String getRank(String name) {
        IChatComponent component = mc().getNetHandler().getPlayerInfo(name).getDisplayName();
        if (component != null) {
            String[] parts = component.getUnformattedText().split("\u2503");
            if (parts.length > 1) {
                return parts[0].trim();
            }
        }
        return "";
    }

    public static UUID getUUID(String name) {
        for(NetworkPlayerInfo info : mc().getNetHandler().getPlayerInfoMap()) {
            if(info.getGameProfile().getName().equals(name))
                return info.getGameProfile().getId();
            if(info.getDisplayName() != null) {
                String[] parts = info.getDisplayName().getUnformattedText().split("\u2503");
                if(parts.length > 1 && parts[1].trim().equals(name)) {
                    return info.getGameProfile().getId();
                }
            }
        }
        return null;
    }

    public static UUID getUUID() {
        return mc().getSession().getProfile().getId();
    }

    public static String getName() {
        return mc().getSession().getUsername();
    }
}
