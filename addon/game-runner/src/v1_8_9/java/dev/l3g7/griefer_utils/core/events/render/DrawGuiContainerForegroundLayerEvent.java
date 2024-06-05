/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events.render;

import dev.l3g7.griefer_utils.core.api.event.event_bus.Event;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

public class DrawGuiContainerForegroundLayerEvent extends Event {

	public final GuiContainer container;

	public DrawGuiContainerForegroundLayerEvent(GuiContainer container) {
		this.container = container;
	}

	@Mixin(GuiContainer.class)
	private static class MixinGuiContainer {

		@Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGuiContainerForegroundLayer(II)V"))
		public void injectDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
			new DrawGuiContainerForegroundLayerEvent(c(this)).fire();
		}

	}


}