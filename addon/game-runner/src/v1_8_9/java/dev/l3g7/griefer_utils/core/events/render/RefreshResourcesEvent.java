package dev.l3g7.griefer_utils.core.events.render;

import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import net.minecraft.client.resources.IReloadableResourceManager;

import java.util.concurrent.atomic.AtomicBoolean;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class RefreshResourcesEvent extends Event {

	static {
		IReloadableResourceManager resourceManager = (IReloadableResourceManager) mc().getResourceManager();

		AtomicBoolean isRegistered = new AtomicBoolean();

		resourceManager.registerReloadListener(iResourceManager -> {
			if (isRegistered.get()) // When registering a reload listener, it's immediately called.
				new RefreshResourcesEvent().fire();
		});

		isRegistered.set(true);
	}

}
