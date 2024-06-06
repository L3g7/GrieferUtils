/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events;

import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

public class TileEntityDataSetEvent extends Event {

	public final TileEntity tileEntity;

	public TileEntityDataSetEvent(TileEntity tileEntity) {
		this.tileEntity = tileEntity;
	}

	@Mixin(NetHandlerPlayClient.class)
	private static class MixinNetHandlerPlayClient {

		@Inject(method = "handleUpdateTileEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
		public void inject(S35PacketUpdateTileEntity packetIn, CallbackInfo ci) {
			new TileEntityDataSetEvent(world().getTileEntity(packetIn.getPos())).fire();
		}

	}

}
