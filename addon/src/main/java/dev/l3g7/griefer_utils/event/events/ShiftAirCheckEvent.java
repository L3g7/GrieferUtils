/*
 * This file is part of GrieferUtils https://github.com/L3g7/GrieferUtils.
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.event.events;

import dev.l3g7.griefer_utils.core.event_bus.Event.TypedEvent;
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
