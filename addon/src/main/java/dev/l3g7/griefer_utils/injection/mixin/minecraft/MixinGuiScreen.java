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

package dev.l3g7.griefer_utils.injection.mixin.minecraft;

import dev.l3g7.griefer_utils.Main;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.core.util.Util;
import dev.l3g7.griefer_utils.event.events.GuiInitEvent;
import dev.l3g7.griefer_utils.event.events.render.RenderToolTipEvent;
import dev.l3g7.griefer_utils.settings.FocusableSetting;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.AddonElement;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Mixin(GuiScreen.class)
public abstract class MixinGuiScreen {

	@Shadow
	public abstract void initGui();

	@Shadow
	protected abstract void actionPerformed(GuiButton button) throws IOException;

	@Inject(method = "renderToolTip", at = @At("HEAD"), cancellable = true)
	public void injectRenderTooltip(ItemStack stack, int x, int y, CallbackInfo ci) {
		if (MinecraftForge.EVENT_BUS.post(new RenderToolTipEvent(stack, (GuiScreen) (Object) this, x, y)))
			ci.cancel();
	}

	@Inject(method = "initGui", at = @At("HEAD"))
	public void injectInitGui(CallbackInfo ci) {
		MinecraftForge.EVENT_BUS.post(new GuiInitEvent((GuiScreen) (Object) this));
	}

	@Inject(method = "keyTyped", at = @At("HEAD"), cancellable = true)
	public void injectKeyTyped(char typedChar, int keyCode, CallbackInfo ci) {
		if (!((Object) this instanceof LabyModAddonsGui))
			return;

		AddonElement openedAddonSettings = Reflection.get(this, "openedAddonSettings");

		if (openedAddonSettings == null || openedAddonSettings.getAddonInfo().getUuid() != Main.getInstance().about.uuid) // Check if GrieferUtils is open
			return;

		if (typedChar != '\t' && typedChar != '\b')
			return;

		ci.cancel();
		List<SettingsElement> tempElementsStored = Reflection.get(this, "tempElementsStored");

		if (typedChar == '\b') {
			for (SettingsElement setting : tempElementsStored)
				if (setting instanceof FocusableSetting && ((FocusableSetting) setting).isFocused())
					return;

			try {
				actionPerformed(Reflection.get(this, "buttonBack"));
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
