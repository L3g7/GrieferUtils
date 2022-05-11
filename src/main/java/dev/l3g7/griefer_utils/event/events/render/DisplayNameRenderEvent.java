package dev.l3g7.griefer_utils.event.events.render;

import dev.l3g7.griefer_utils.event.event_bus.Event;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IChatComponent;

public class DisplayNameRenderEvent extends Event {

    private IChatComponent displayName;
    private final EntityPlayer player;

    public DisplayNameRenderEvent(IChatComponent original, EntityPlayer player) {
        displayName = original.createCopy();
        this.player = player;
    }

    public IChatComponent getDisplayName() {
        return displayName;
    }

    public void setDisplayName(IChatComponent displayName) {
        this.displayName = displayName;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

}
