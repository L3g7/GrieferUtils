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

import dev.l3g7.griefer_utils.core.event_bus.Event;
import net.labymod.user.User;
import net.labymod.user.group.LabyGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.core.reflection.Reflection.c;

public class UserSetGroupEvent extends Event {

	public final User user;
	public final LabyGroup group;

	public UserSetGroupEvent(User user, LabyGroup group) {
		this.user = user;
		this.group = group;
	}

	@Mixin(value = User.class, remap = false)
	private static class MixinUser {

		@Inject(method = "setGroup", at = @At("HEAD"), cancellable = true)
		public void injectSetGroup(LabyGroup group, CallbackInfo ci) {
			if (new UserSetGroupEvent(c(this), group).fire().isCanceled())
				ci.cancel();
		}

	}

}
