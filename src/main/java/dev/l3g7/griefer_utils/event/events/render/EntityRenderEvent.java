package dev.l3g7.griefer_utils.event.events.render;

import dev.l3g7.griefer_utils.event.event_bus.Event;
import net.minecraft.entity.Entity;

public class EntityRenderEvent extends Event {

    private final Entity entity;
    private final double x, y, z;

    public EntityRenderEvent(Entity entity, double x, double y, double z) {
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Entity getEntity() {
        return entity;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }


}
