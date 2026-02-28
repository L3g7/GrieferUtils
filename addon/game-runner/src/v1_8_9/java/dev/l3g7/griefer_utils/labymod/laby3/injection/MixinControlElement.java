/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.injection;

import dev.l3g7.griefer_utils.core.settings.AbstractSetting;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.manager.TooltipHelper;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(value = ControlElement.class, remap = false)
public abstract class MixinControlElement {

	@Shadow
	private boolean settingEnabled;
	private static final int NEW_BADGE_WIDTH = 20;
	private static final int NEW_BADGE_HEIGHT = 9;

	@Inject(method = "draw", at = @At(value = "INVOKE", target = "Lnet/labymod/settings/elements/ControlElement;renderAdvancedButton(IIIIZII)V", shift = AFTER))
	private void injectDraw(int x, int y, int maxX, int maxY, int mouseX, int mouseY, CallbackInfo ci) {
		if (!(this instanceof AbstractSetting<?, ?> setting) || setting.since() == null)
			return;

		x -= NEW_BADGE_WIDTH + 6;
		y += (maxY - y - NEW_BADGE_HEIGHT) / 2;
		Gui.drawRect(x, y, x + NEW_BADGE_WIDTH, y + NEW_BADGE_HEIGHT, 0xFF_0000FF);

		mc().getTextureManager().bindTexture(new ResourceLocation("griefer_utils", "textures/new_badge.png"));
		LabyMod.getInstance().getDrawUtils().drawTexture(x, y, 0, 0, 256, 256, 20, 9, 1);

		if (x <= mouseX && mouseX <= x + NEW_BADGE_WIDTH && y <= mouseY && mouseY <= y + NEW_BADGE_HEIGHT)
			TooltipHelper.getHelper().pointTooltip(mouseX, mouseY, 0L, new String[]{ setting.since().toString() });
	}

}
