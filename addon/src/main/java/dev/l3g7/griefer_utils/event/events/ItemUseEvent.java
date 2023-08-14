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

package dev.l3g7.griefer_utils.event.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

public class ItemUseEvent extends Event {

	private final ItemStack stackBeforeUse;
	private final ItemStack stackAfteruse;

	public ItemUseEvent(ItemStack stackBeforeUse, ItemStack stackAfteruse) {
		this.stackBeforeUse = stackBeforeUse;

		if (stackAfteruse != null && stackAfteruse.stackSize <= 0)
			stackAfteruse = null;

		this.stackAfteruse = stackAfteruse;
	}

	public ItemStack getStackBeforeUse() {
		return stackBeforeUse;
	}

	public ItemStack getStackAfteruse() {
		return stackAfteruse;
	}

	@Mixin(ItemStack.class)
	private static class MixinItemStack {

		private ItemStack stackBeforeUse = null;

		@Inject(method = "onItemUse", at = @At("HEAD"))
		public void injectOnItemUseHead(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir) {
			stackBeforeUse = ((ItemStack) (Object) this).copy();
		}

		@Inject(method = "onItemUse", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
		public void injectOnItemUseTail(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir, boolean flag) {
			if (flag) {
				MinecraftForge.EVENT_BUS.post(new ItemUseEvent(stackBeforeUse, ((ItemStack) (Object) this).copy()));
			}
		}

	}

	public static class Finish extends Event {

		@Mixin(EntityPlayer.class)
		private static class MixinEntityPlayer {
			@Redirect(method = "onItemUseFinish", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;onItemUseFinish(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;"))
			public ItemStack redirectOnItemUseFinish(ItemStack instance, World worldIn, EntityPlayer playerIn) {
				MinecraftForge.EVENT_BUS.post(new Finish());
				return instance.onItemUseFinish(worldIn, playerIn);
			}
		}

	}


}
