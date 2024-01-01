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

import dev.l3g7.griefer_utils.core.event_bus.Event.TypedEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.l3g7.griefer_utils.core.reflection.Reflection.c;

/**
 * An event being posted when a player's display name is read.
 */
public class DisplayNameGetEvent extends TypedEvent<DisplayNameGetEvent> {

	public final EntityPlayer player;
	public IChatComponent displayName;

	public DisplayNameGetEvent(EntityPlayer player, IChatComponent displayName) {
		this.player = player;
		this.displayName = displayName;
	}

	@Mixin(EntityPlayer.class)
	private static class MixinEntityPlayer {

		@Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
		private void injectGetDisplayName(CallbackInfoReturnable<IChatComponent> cir) {
			DisplayNameGetEvent event = new DisplayNameGetEvent(c(this), cir.getReturnValue()).fire();
			cir.setReturnValue(event.displayName);
		}

	}

}
