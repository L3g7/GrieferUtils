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

package dev.l3g7.griefer_utils.injection.mixin.labymod;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.features.chat.chat_menu.ChatMenu;
import net.labymod.core_implementation.mc18.MinecraftImplementation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MinecraftImplementation.class, remap = false)
public class MixinMinecraftImplementation {

	@Inject(method = "getClickEventValue", at = @At("HEAD"), cancellable = true)
	public void injectGetClickEventValue(int x, int y, CallbackInfoReturnable<String> cir) {
		if (FileProvider.getSingleton(ChatMenu.class).isEnabled())
			cir.setReturnValue(null);
	}

}