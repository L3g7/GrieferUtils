package dev.l3g7.griefer_utils.event.events.network.tablist;

import com.mojang.authlib.GameProfile;
import dev.l3g7.griefer_utils.event.event_bus.Event;
import dev.l3g7.griefer_utils.event.event_bus.EventBus;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.network.PacketReceiveEvent;
import dev.l3g7.griefer_utils.util.Reflection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.util.IChatComponent;

public class TabListNameUpdateEvent extends Event {

    private final GameProfile profile;
    private IChatComponent component;

    public TabListNameUpdateEvent(GameProfile profile, IChatComponent original) {
        this.profile = profile;
        this.component = original.createCopy();
    }

    public TabListNameUpdateEvent(S38PacketPlayerListItem.AddPlayerData data) {
        this(data.getProfile(), data.getDisplayName());
    }

    public TabListNameUpdateEvent(GameProfile profile) {
        this(profile, TabListEvent.cachedComponents.get(profile.getId()));
    }

    public GameProfile getProfile() {
        return profile;
    }

    public IChatComponent getComponent() {
        return component;
    }

    public void setComponent(IChatComponent component) {
        this.component = component;
    }

    @EventListener
    public static void onPacket(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S38PacketPlayerListItem) {
            S38PacketPlayerListItem packet = (S38PacketPlayerListItem) event.getPacket();

            switch (packet.func_179768_b()) {
                case ADD_PLAYER:
                case UPDATE_DISPLAY_NAME: {
                    for (S38PacketPlayerListItem.AddPlayerData data : packet.func_179767_a()) {
                        if (data.getDisplayName() == null)
                            continue;

                        TabListEvent.cachedComponents.put(data.getProfile().getId(), data.getDisplayName());
                        IChatComponent component = EventBus.post(new TabListNameUpdateEvent(data)).getComponent(); // Post TabListNameUpdateEvent
                        Reflection.set(data, component, "displayName", "field_179965_e", "e"); // Update displayName
                    }
                }
            }
        }
    }

    public static void updateTabListNames() {
        if (Minecraft.getMinecraft().getNetHandler() == null)
            return;

        for (NetworkPlayerInfo info : Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap()) {
            if(info.getDisplayName() == null)
                continue;
            IChatComponent newComponent = EventBus.post(new TabListNameUpdateEvent(info.getGameProfile())).getComponent();
            info.setDisplayName(newComponent);
        }
    }
}
