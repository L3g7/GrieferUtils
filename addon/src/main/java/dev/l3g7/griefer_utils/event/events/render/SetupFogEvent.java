/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

import dev.l3g7.griefer_utils.core.event_bus.Event;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static dev.l3g7.griefer_utils.event.events.render.SetupFogEvent.FogType.*;

/**
 * An event being posted when EntityRenderer#setupFog is called.
 */
public class SetupFogEvent extends Event {

	public final FogType fogType;

	public SetupFogEvent(FogType fogType) {
		this.fogType = fogType;
	}

	public enum FogType {
		BLINDNESS, WATER, LAVA
	}

	@Mixin(EntityRenderer.class)
	private static class MixinEntityRenderer {

		@Redirect(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;isPotionActive(Lnet/minecraft/potion/Potion;)Z"))
		private boolean redirectIsPotionActive(EntityLivingBase instance, Potion potionIn) {
			boolean isPotionActive = instance.isPotionActive(potionIn);

			if (isPotionActive && potionIn == Potion.blindness)
				return !new SetupFogEvent(BLINDNESS).fire().isCanceled();

			return isPotionActive;
		}

		@Redirect(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;getBlockAtEntityViewpoint(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;F)Lnet/minecraft/block/Block;"))
		private Block redirectGetBlockAtEntityViewpoint(World world, Entity renderViewEntity, float partialTicks) {
			Block viewBlock = ActiveRenderInfo.getBlockAtEntityViewpoint(world, renderViewEntity, partialTicks);
			return shouldCancelBlockFog(viewBlock) ? Blocks.air : viewBlock;
		}

		private boolean shouldCancelBlockFog(Block block) {

			if (block.getMaterial() == Material.water)
				return new SetupFogEvent(WATER).fire().isCanceled();

			else if (block.getMaterial() == Material.lava)
				return new SetupFogEvent(LAVA).fire().isCanceled();

			return false;
		}

	}

}
