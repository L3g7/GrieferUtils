/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events;

import dev.l3g7.griefer_utils.core.api.event.event_bus.Event.TypedEvent;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

public class ShiftAirCheckEvent extends TypedEvent<ShiftAirCheckEvent> {

	public double boundingBoxOffset;

	public ShiftAirCheckEvent(double boundingBoxOffset) {
		this.boundingBoxOffset = boundingBoxOffset;
	}

	@Mixin(Entity.class)
	private static class MixinEntity {

		@Redirect(method = "moveEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/AxisAlignedBB;offset(DDD)Lnet/minecraft/util/AxisAlignedBB;"), slice = @Slice(
			from = @At(value = "INVOKE:FIRST", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V"),
			to = @At(value = "INVOKE:FIRST", target = "Lnet/minecraft/util/AxisAlignedBB;addCoord(DDD)Lnet/minecraft/util/AxisAlignedBB;")))
		public AxisAlignedBB injectMoveEntity(AxisAlignedBB instance, double x, double y, double z) {
			return instance.offset(x, new ShiftAirCheckEvent(y).fire().boundingBoxOffset, z);
		}

	}

}
