/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.settings;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.misc.SkullMaterial;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.labymod.api.Laby;
import net.labymod.api.Textures;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.core.client.render.schematic.Schematic;
import net.labymod.core.client.render.schematic.block.Block;
import net.labymod.core.configuration.labymod.LabyConfigProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStream;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;

@Singleton
@ExclusiveTo(LABY_4)
public class MainMenuSkull {

	private static Boolean firstEnabled;

	public static final SwitchSetting enabled = SwitchSetting.create()
		.name("GrieferUtils-Kopf hinzufügen")
		.description("Fügt in der Hintergrundwelt der Startseite einen GrieferUtils-Kopf hinzu.")
		.icon("skull")
		.callback(active -> {
			if (firstEnabled == null) {
				firstEnabled = active;
				return;
			}

			if (active != firstEnabled) {
				String action = active ? "hinzuzufügen" : "zu entfernen";
				labyBridge.notify("§e§lNeustart benötigt ⚠", "Um den Kopf " + action + ", muss Minecraft neugestartet werden.");
			}
		})
		.config("settings.main_menu_skull")
		.defaultValue(true);

	static {
		LabyConfigProvider.INSTANCE.get().appearance().dynamicBackground().enabled().addChangeListener(enabled::enabled);
	}

	@Mixin(value = Schematic.class, remap = false)
	@ExclusiveTo(LABY_4)
	private abstract static class MixinSchematic {

		@Shadow
		@Final
		private Int2ObjectMap<Block> palette;

		@Shadow
		protected abstract int getIndex(int x, int y, int z);

		@Shadow
		@Final
		private byte[] blocks;

		@Inject(method = "<init>(Ljava/io/InputStream;)V", at = @At("TAIL"))
		private void injectInit(InputStream inputStream, CallbackInfo ci) {
			if (!enabled.get())
				return;

			blocks[getIndex(10, 13, 19)] = (byte) 255;
			palette.put(255, new Block("griefer_utils", new SkullMaterial()));
		}

	}

	@Mixin(value = Textures.Splash.class, remap = false)
	@ExclusiveTo(LABY_4)
	private static class MixinTextures {

		@Mutable
		@Shadow
		@Final
		public static ResourceLocation BLOCKS;

		@Inject(method = "<clinit>", at = @At("TAIL"))
		private static void inject(CallbackInfo ci) {
			if (!enabled.get())
				return;

			BLOCKS = Laby.labyAPI()
				.renderPipeline()
				.resources()
				.resourceLocationFactory()
				.create("griefer_utils", "textures/blocks.png");
		}

	}


}
