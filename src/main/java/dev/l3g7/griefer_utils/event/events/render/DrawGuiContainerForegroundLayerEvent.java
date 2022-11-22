package dev.l3g7.griefer_utils.event.events.render;

import dev.l3g7.griefer_utils.event.event_bus.Event;
import net.minecraft.client.gui.inventory.GuiChest;

public class DrawGuiContainerForegroundLayerEvent extends Event {

	public final GuiChest chest;

	public DrawGuiContainerForegroundLayerEvent(GuiChest chest) {
		this.chest = chest;
	}

}
