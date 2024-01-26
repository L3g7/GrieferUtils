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

package dev.l3g7.griefer_utils.v1_8_9.events;

import dev.l3g7.griefer_utils.api.event.event_bus.Event;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.player;

public class WindowClickEvent extends Event {

	public final int windowId;
	public final int slotId;
	public final int mouseButtonClicked;
	public final int mode;
	public ItemStack itemStack;

	public WindowClickEvent(int windowId, int slotId, int mouseButtonClicked, int mode) {
		this.windowId = windowId;
		this.slotId = slotId;
		this.mouseButtonClicked = mouseButtonClicked;
		this.mode = mode;

		if (slotId == -999)
			return;

		List<Slot> slots = player().openContainer.inventorySlots;
		if (slotId < 0 || slotId >= slots.size())
			return;

		itemStack = slots.get(slotId).getStack();
	}

	@Mixin(PlayerControllerMP.class)
	private static class MixinPlayerControllerMP {

		@Inject(method = "windowClick", at = @At("HEAD"), cancellable = true)
		public void injectWindowClick(int windowId, int slotId, int mouseButtonClicked, int mode, EntityPlayer playerIn, CallbackInfoReturnable<ItemStack> cir) {
			if (new WindowClickEvent(windowId, slotId, mouseButtonClicked, mode).fire().isCanceled())
				cir.cancel();
		}

	}

}
