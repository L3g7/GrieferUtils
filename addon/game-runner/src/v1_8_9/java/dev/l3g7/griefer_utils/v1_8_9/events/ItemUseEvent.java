/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.events;

import dev.l3g7.griefer_utils.api.event.event_bus.Event;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

public abstract class ItemUseEvent extends Event {

	public static class Pre extends ItemUseEvent {

		public final ItemStack stack;
		public final EntityPlayer playerIn;
		public final World worldIn;
		public final BlockPos pos;
		public final EnumFacing side;
		public final float hitX;
		public final float hitY;
		public final float hitZ;

		public Pre(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
			this.stack = stack;
			this.playerIn = playerIn;
			this.worldIn = worldIn;
			this.pos = pos;
			this.side = side;
			this.hitX = hitX;
			this.hitY = hitY;
			this.hitZ = hitZ;
		}

		@Mixin(PlayerControllerMP.class)
		private static class MixinPlayerControllerMP {

			@Inject(method = "onPlayerRightClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getBlockState(Lnet/minecraft/util/BlockPos;)Lnet/minecraft/block/state/IBlockState;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION)
			public void injectOnPlayerRightClick(EntityPlayerSP player, WorldClient worldIn, ItemStack heldStack, BlockPos hitPos, EnumFacing side, Vec3 hitVec, CallbackInfoReturnable<Boolean> cir, float f, float f1, float f2) {
				if (new Pre(heldStack, player, worldIn, hitPos, side, f, f1, f2).fire().isCanceled())
					cir.setReturnValue(false);
			}

		}

	}

	public static class Post extends ItemUseEvent {

		public final ItemStack stackBeforeUse;
		public final ItemStack stackAfterUse;

		public Post(ItemStack stackBeforeUse, ItemStack stackAfterUse) {
			this.stackBeforeUse = stackBeforeUse;

			if (stackAfterUse != null && stackAfterUse.stackSize <= 0)
				stackAfterUse = null;

			this.stackAfterUse = stackAfterUse;
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
				if (flag)
					new Post(stackBeforeUse, ((ItemStack) (Object) this).copy()).fire();
			}

			@Inject(method = "useItemRightClick", at = @At("HEAD"))
			private void injectUseItemRightClickHead(World worldIn, EntityPlayer playerIn, CallbackInfoReturnable<ItemStack> cir) {
				stackBeforeUse = ((ItemStack) (Object) this).copy();
			}

			@Inject(method = "useItemRightClick", at = @At("RETURN"))
			private void injectUseItemRightClickTail(World worldIn, EntityPlayer playerIn, CallbackInfoReturnable<ItemStack> cir) {
				new Post(stackBeforeUse, cir.getReturnValue().copy()).fire();
			}

		}

	}

	public static class Finish extends ItemUseEvent {

		@Mixin(EntityPlayer.class)
		private static class MixinEntityPlayer {
			@Redirect(method = "onItemUseFinish", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;onItemUseFinish(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;"))
			public ItemStack redirectOnItemUseFinish(ItemStack instance, World worldIn, EntityPlayer playerIn) {
				new Finish().fire();
				return instance.onItemUseFinish(worldIn, playerIn);
			}
		}

	}


}
