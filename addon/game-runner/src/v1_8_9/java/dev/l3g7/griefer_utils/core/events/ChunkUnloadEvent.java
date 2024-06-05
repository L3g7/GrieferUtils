/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events;

import dev.l3g7.griefer_utils.core.api.event.event_bus.Event;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

public class ChunkUnloadEvent extends Event {

	public final Chunk chunk;

	public ChunkUnloadEvent(Chunk chunk) {
		this.chunk = chunk;
	}

	@Mixin(Chunk.class)
	private static class MixinChunk {

	    @Inject(method = "onChunkUnload", at = @At("HEAD"))
	    public void injectonChunkUnload(CallbackInfo ci) {
		    new ChunkUnloadEvent(c(this)).fire();
	    }

	}

}
