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

package dev.l3g7.griefer_utils.event.events;

import dev.l3g7.griefer_utils.core.event_bus.Event;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class BlockEvent extends Event {

	public final BlockPos pos;

	public BlockEvent(BlockPos pos) {
		this.pos = pos;
	}

	public static class BlockInteractEvent extends BlockEvent {

		public BlockInteractEvent(BlockPos pos) {
			super(pos);
		}

		@Mixin(Minecraft.class)
		private static abstract class MixinMinecraft {

			@Redirect(method = "rightClickMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;onPlayerRightClick(Lnet/minecraft/client/entity/EntityPlayerSP;Lnet/minecraft/client/multiplayer/WorldClient;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/util/Vec3;)Z"))
			public boolean redirectPlayerRightClick(PlayerControllerMP instance, EntityPlayerSP player, WorldClient worldIn, ItemStack heldStack, BlockPos hitPos, EnumFacing side, Vec3 hitVec) {
				if (new BlockInteractEvent(hitPos).fire().isCanceled())
					return false;

				return instance.onPlayerRightClick(player, worldIn, heldStack, hitPos, side, hitVec);
			}

		}

	}

	public static class BlockClickEvent extends BlockEvent {

		public BlockClickEvent(BlockPos pos) {
			super(pos);
		}

		@Mixin(PlayerControllerMP.class)
		private static class MixinPlayerControllerMP {

			@Inject(method = "clickBlock", at = @At("HEAD"), cancellable = true)
			private void injectClickBlock(BlockPos loc, EnumFacing face, CallbackInfoReturnable<Boolean> cir) {
				if (new BlockClickEvent(loc).fire().isCanceled())
					cir.setReturnValue(false);
			}

		}

	}

	public static class BlockBrokeEvent extends BlockEvent {

		public final EnumFacing side;

		public BlockBrokeEvent(BlockPos pos, EnumFacing side) {
			super(pos);
			this.side = side;
		}

		@Mixin(PlayerControllerMP.class)
		private static class MixinPlayerControllerMP {

			@Inject(method = "onPlayerDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onBlockDestroyedByPlayer(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;)V", shift = At.Shift.BEFORE))
			private void injectOnPlayerDestroyBlock(BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> cir) {
				new BlockBrokeEvent(pos, side).fire();
			}

		}

	}

}
