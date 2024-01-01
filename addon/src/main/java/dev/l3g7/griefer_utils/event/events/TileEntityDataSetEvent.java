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
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.world;

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
