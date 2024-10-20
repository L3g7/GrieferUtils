/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.injection;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.injection.InheritedInvoke;
import dev.l3g7.griefer_utils.labymod.laby3.settings.MainPage;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;

@ExclusiveTo(LABY_3)
@Mixin(value = LabyModAddonsGui.class, remap = false)
public class MixinLabyModAddonsGui {

	@Shadow
	private GuiButton buttonBack;

	@Shadow
	private ArrayList<SettingsElement> path;

	@InheritedInvoke(GuiScreen.class)
	@Inject(method = "actionPerformed", at = @At("HEAD"), remap = true)
	private void injectActionPerformed(GuiButton button, CallbackInfo ci) {
		if (button != buttonBack || path.size() > 1)
			return;

		MainPage.filter.set(path.isEmpty() ? "" : MainPage.filter.get());
	}

}
