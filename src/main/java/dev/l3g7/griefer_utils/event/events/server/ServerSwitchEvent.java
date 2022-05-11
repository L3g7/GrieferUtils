package dev.l3g7.griefer_utils.event.events.server;

import dev.l3g7.griefer_utils.event.event_bus.Event;
import dev.l3g7.griefer_utils.event.event_bus.EventBus;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.network.PacketReceiveEvent;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

public class ServerSwitchEvent extends Event {

    @EventListener
    public static void onPacket(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S3FPacketCustomPayload) {
            if (((S3FPacketCustomPayload) event.getPacket()).getChannelName().equals("MC|Brand"))
                EventBus.post(new ServerSwitchEvent());
        }
    }

}
