package dev.l3g7.griefer_utils.event.events.network.tablist;

import dev.l3g7.griefer_utils.event.event_bus.EventBus;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.network.PacketReceiveEvent;
import net.minecraft.network.play.server.S38PacketPlayerListItem;

public class TabListAddPlayerEvent extends TabListEvent {

    public TabListAddPlayerEvent(S38PacketPlayerListItem.AddPlayerData data) {
        super(data);
        cachedNames.put(data.getProfile().getId(), data.getProfile().getName());
    }

    @EventListener
    public static void onPacket(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S38PacketPlayerListItem) {
            S38PacketPlayerListItem packet = (S38PacketPlayerListItem) event.getPacket();

            if (packet.func_179768_b() == S38PacketPlayerListItem.Action.ADD_PLAYER) {
                for (S38PacketPlayerListItem.AddPlayerData data : packet.func_179767_a()) {
                    if (data.getDisplayName() != null && data.getProfile().getName() != null && !data.getProfile().getName().equals("null"))
                        EventBus.post(new TabListAddPlayerEvent(data)); // Post TabListAddPlayerEvent
                }
            }
        }
    }
}
