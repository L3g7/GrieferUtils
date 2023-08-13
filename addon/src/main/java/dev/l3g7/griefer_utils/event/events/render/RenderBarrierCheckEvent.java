/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package dev.l3g7.griefer_utils.event.events.render;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * An event being posted when {@link WorldClient#doVoidFogParticles(int, int, int)} checks whether barriers should be rendered.
 */
@Cancelable
public class RenderBarrierCheckEvent extends Event {

	@Mixin(WorldClient.class)
	private static class MixinWorldClient {

		@ModifyVariable(method = "doVoidFogParticles", at = @At("STORE"), ordinal = 0)
		private boolean modifyShouldRenderBarrier(boolean shouldRenderBarrier) {
			return shouldRenderBarrier || MinecraftForge.EVENT_BUS.post(new RenderBarrierCheckEvent());
		}

	}

}
