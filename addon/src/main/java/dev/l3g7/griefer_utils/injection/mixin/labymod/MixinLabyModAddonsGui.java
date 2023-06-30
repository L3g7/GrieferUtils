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

import dev.l3g7.griefer_utils.Main;
import dev.l3g7.griefer_utils.core.util.Util;
import dev.l3g7.griefer_utils.settings.FocusableSetting;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.AddonElement;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.gui.GuiButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Mixin(LabyModAddonsGui.class)
public abstract class MixinLabyModAddonsGui {

	@Shadow
	private AddonElement openedAddonSettings;

	@Shadow
	protected abstract void actionPerformed(GuiButton button) throws IOException;

	@Shadow
	private GuiButton buttonBack;

	@Shadow
	private List<SettingsElement> tempElementsStored;

	@Shadow
	public abstract void initGui();

	@Inject(method = "keyTyped", at = @At("HEAD"), cancellable = true)
	public void injectKeyTyped(char typedChar, int keyCode, CallbackInfo ci) {
		if (openedAddonSettings == null || openedAddonSettings.getAddonInfo().getUuid() != Main.getInstance().about.uuid) // Check if GrieferUtils is open
			return;

		if (typedChar != '\t' && typedChar != '\b')
			return;

		ci.cancel();

		if (typedChar == '\b') {
			try {
				actionPerformed(buttonBack);
			} catch (IOException e) {
				throw Util.elevate(e);
			}
			return;
		}

		List<FocusableSetting> settings = new ArrayList<>();
		for (SettingsElement setting : tempElementsStored)
			if (setting instanceof FocusableSetting)
				settings.add((FocusableSetting) setting);

		if (settings.isEmpty())
			return;

		int focusedTextField = 0;
		for (FocusableSetting setting : settings) {
			if (setting.isFocused()) {
				if (settings.size() == 1)
					return;

				setting.setFocused(false);
				break;
			}

			focusedTextField++;
		}

		settings.get(focusedTextField == settings.size() ? 0 : (focusedTextField + 1) % settings.size()).setFocused(true);
	}

}
