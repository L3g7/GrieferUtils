package dev.l3g7.griefer_utils.event.events.render;

import dev.l3g7.griefer_utils.event.event_bus.Event;
import net.minecraft.entity.Entity;


public class RenderBurningCheckEvent extends Event.Cancelable {

    private final Entity entity;

    public RenderBurningCheckEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

}
