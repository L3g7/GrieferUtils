package dev.l3g7.griefer_utils.event.events.network.tablist;

import dev.l3g7.griefer_utils.event.event_bus.Event;
import dev.l3g7.griefer_utils.features.tweaks.autounnick.NameCache;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.util.IChatComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TabListEvent extends Event {

    public static final Map<UUID, IChatComponent> cachedComponents = new HashMap<>();

    private final S38PacketPlayerListItem.AddPlayerData data;

    TabListEvent(S38PacketPlayerListItem.AddPlayerData data) {
        this.data = data;
    }

    public String getName() {
        return NameCache.getName(data);
    }
}
