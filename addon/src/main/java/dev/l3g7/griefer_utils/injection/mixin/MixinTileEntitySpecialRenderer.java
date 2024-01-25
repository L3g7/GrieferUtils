/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.injection.mixin;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Mixin(TileEntitySpecialRenderer.class)
public class MixinTileEntitySpecialRenderer {

	@Shadow
	protected TileEntityRendererDispatcher rendererDispatcher;

	@Inject(method = "setRendererDispatcher", at = @At("RETURN"))
	public void injectSetRendererDispatcher(CallbackInfo ci) {
		rendererDispatcher.renderEngine = mc().getTextureManager();
	}

}
