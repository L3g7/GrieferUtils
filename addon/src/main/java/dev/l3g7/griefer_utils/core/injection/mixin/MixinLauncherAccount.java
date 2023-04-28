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

package dev.l3g7.griefer_utils.core.injection.mixin;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.features.player.LabyModSwitcherFix;
import net.labymod.accountmanager.storage.loader.microsoft.model.LauncherAccount;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LauncherAccount.class)
public class MixinLauncherAccount {

	@Inject(method = "getAccessToken", at = @At("RETURN"), cancellable = true, remap = false)
	public void injectGetAccessToken(CallbackInfoReturnable<String> cir) {
		String accessToken = cir.getReturnValue();
		if (accessToken == null || accessToken.startsWith("ey") || !FileProvider.getSingleton(LabyModSwitcherFix.class).isEnabled())
			return;

		if (accessToken.startsWith("8E184B2C-7E2D-4517-A905-623B1BE84B5700000001ffffffffffffffffey")) {
			cir.setReturnValue(accessToken.substring(60));
			return;
		}

		cir.setReturnValue(accessToken.split("\\.")[0]);
	}

}
