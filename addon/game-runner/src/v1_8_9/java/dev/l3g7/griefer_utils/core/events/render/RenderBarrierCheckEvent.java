/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events.render;

import dev.l3g7.griefer_utils.core.api.event.event_bus.Event.TypedEvent;
import net.minecraft.client.multiplayer.WorldClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * An event being posted when {@link WorldClient#doVoidFogParticles(int, int, int)} checks whether barriers should be rendered.
 */
public class RenderBarrierCheckEvent extends TypedEvent<RenderBarrierCheckEvent> {

	public boolean renderBarrier;

	public RenderBarrierCheckEvent(boolean renderBarrier) {
		this.renderBarrier = renderBarrier;
	}

	@Mixin(WorldClient.class)
	private static class MixinWorldClient {

		@ModifyVariable(method = "doVoidFogParticles", at = @At("STORE"), ordinal = 0)
		private boolean modifyShouldRenderBarrier(boolean shouldRenderBarrier) {
			return new RenderBarrierCheckEvent(shouldRenderBarrier).fire().renderBarrier;
		}

	}

}
