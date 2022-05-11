package dev.l3g7.griefer_utils.event.events.render;

import dev.l3g7.griefer_utils.event.event_bus.Event;
import net.minecraft.entity.Entity;

public class RenderInvisibilityCheckEvent extends Event {

    private final Entity entity;
    private boolean shouldRender = false;

    public RenderInvisibilityCheckEvent(Entity entity) {
        this.entity = entity;
    }

    public void render() {
        shouldRender = true;
    }

    public boolean shouldRender() {
        return shouldRender;
    }

    public Entity getEntity() {
        return entity;
    }

}
