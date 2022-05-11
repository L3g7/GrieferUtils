package dev.l3g7.griefer_utils.event.events.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.l3g7.griefer_utils.event.event_bus.Event;
import dev.l3g7.griefer_utils.event.event_bus.EventBus;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

public class MMCustomPayloadEvent extends Event {

    private static final JsonParser JSON_PARSER = new JsonParser();

    private final String channel;
    private final JsonElement payload;

    public MMCustomPayloadEvent(PacketBuffer bufferData) {
        bufferData.markReaderIndex();
        this.channel = bufferData.readStringFromBuffer(65536);
        this.payload = JSON_PARSER.parse(bufferData.readStringFromBuffer(65536));
        bufferData.resetReaderIndex();
    }

    public String getChannel() {
        return channel;
    }

    public JsonElement getPayload() {
        return payload;
    }

    @EventListener
    public static void onPacket(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S3FPacketCustomPayload) {
            if (((S3FPacketCustomPayload) event.getPacket()).getChannelName().equals("mysterymod:mm"))
                EventBus.post(new MMCustomPayloadEvent(((S3FPacketCustomPayload) event.getPacket()).getBufferData()));
        }
    }

}
