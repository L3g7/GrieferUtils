package dev.l3g7.griefer_utils.event.events.network.tablist;

import dev.l3g7.griefer_utils.event.event_bus.EventBus;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.network.PacketReceiveEvent;
import net.minecraft.network.play.server.S38PacketPlayerListItem;

public class TabListRemovePlayerEvent extends TabListEvent {

    private final String cachedName;

    public TabListRemovePlayerEvent(S38PacketPlayerListItem.AddPlayerData data) {
        super(data);
        this.cachedName = cachedNames.getOrDefault(data.getProfile().getId(), null);
    }

    @Override
    public String getName() {
        return cachedName;
    }

    @EventListener
    public static void onPacket(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S38PacketPlayerListItem) {
            S38PacketPlayerListItem packet = (S38PacketPlayerListItem) event.getPacket();

            if (packet.func_179768_b() == S38PacketPlayerListItem.Action.REMOVE_PLAYER)
                for (S38PacketPlayerListItem.AddPlayerData data : packet.func_179767_a())
                    EventBus.post(new TabListRemovePlayerEvent(data));
        }
    }

}
