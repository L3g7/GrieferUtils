package dev.l3g7.griefer_utils.event.events.render;

import dev.l3g7.griefer_utils.event.event_bus.Event;


public class RenderFogCheckEvent extends Event.Cancelable {

    private final FogType type;

    public RenderFogCheckEvent(FogType type) {
        this.type = type;
    }

    public RenderFogCheckEvent(int type) {
        this(FogType.values()[type]);
    }

    public FogType getType() {
        return type;
    }

    public enum FogType {

        BLINDNESS, WATER, LAVA

    }

}
