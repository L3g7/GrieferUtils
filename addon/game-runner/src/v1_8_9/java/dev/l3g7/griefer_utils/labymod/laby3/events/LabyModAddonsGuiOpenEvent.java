package dev.l3g7.griefer_utils.labymod.laby3.events;

import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.GuiOpenEvent;
import net.labymod.settings.LabyModAddonsGui;

public class LabyModAddonsGuiOpenEvent extends Event {

	@EventListener
	private static void onGuiOpen(GuiOpenEvent<LabyModAddonsGui> event) {
		new LabyModAddonsGuiOpenEvent().fire();
	}

}
