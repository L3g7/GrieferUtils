/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.world;

import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.init.Items;
import net.minecraft.world.storage.MapData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Singleton
public class BuggedMapsFix extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Verbuggte Karten fixen")
		.description("Behebt, dass Karten das falsche Bild anzeigen.")
		.icon(Items.map);

	@Mixin(MapItemRenderer.class)
	private static class MixinMapItemRenderer {

		@Shadow
		@Final
		private Map<String, Object> loadedMaps;

		@Shadow
		@Final
		private TextureManager textureManager;

		@Inject(method = "updateMapTexture", at = @At("HEAD"))
	    private void injectUpdateMapTexture(MapData mapdataIn, CallbackInfo ci) {
			if (!FileProvider.getSingleton(BuggedMapsFix.class).isEnabled())
				return;

			Object loadedMap = loadedMaps.get(mapdataIn.mapName);
	    	if (loadedMap == null)
				return;

			textureManager.deleteTexture(Reflection.get(loadedMap, "location"));
			loadedMaps.remove(mapdataIn.mapName);
	    }

	}

}
