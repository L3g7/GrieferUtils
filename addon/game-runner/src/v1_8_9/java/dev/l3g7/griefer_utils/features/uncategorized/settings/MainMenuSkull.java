/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.settings;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.api.util.io.IO;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Reason.NOT_POSSIBLE;
import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;

@Singleton
@SuppressWarnings("UnresolvedMixinReference")
@ExclusiveTo(value = LABY_4, reason = NOT_POSSIBLE, customMessage = "der dynamische Hintergrund existiert nur in LabyMod 4.")
public class MainMenuSkull {

	public static Boolean enabledAtStartup;

	public static final SwitchSetting enabled = SwitchSetting.create()
		.name("GrieferUtils-Kopf hinzufügen")
		.description("Fügt in der Hintergrundwelt der Startseite einen GrieferUtils-Kopf hinzu.")
		.icon("skull")
		.callback(active -> {
			if (enabledAtStartup != null && enabledAtStartup != active) {
				String action = active ? "hinzuzufügen" : "zu entfernen";
				labyBridge.notify("§e§lNeustart benötigt ⚠", "Um den Kopf " + action + ", muss Minecraft neugestartet werden.");
			}
		})
		.config("settings.main_menu_skull")
		.defaultValue(true);

	@Pseudo
	@ExclusiveTo(LABY_4)
	@Mixin(targets = "net.labymod.core.client.render.blockscene.schematic.SpongeSchematicCodec", remap = false)
	public static class BlockPlacer {

		@Inject(method = "read", at = @At("RETURN"), require = 0)
		void placeBlock(InputStream input, CallbackInfoReturnable<?> ci) {
			enabledAtStartup = enabled.get();
			if (!enabled.get()) return;

			var blockScene = ci.getReturnValue();
			var registry = Reflection.get(this, "registry");
			var state = Reflection.invoke(registry, "state", "griefer_utils:head[rotation=1]");
			Reflection.invoke(blockScene, "setBlock", 10, 13, 19, state);
		}

	}

	@Pseudo
	@ExclusiveTo(LABY_4)
	@Mixin(targets = "net.labymod.core.client.render.blockscene.model.vanilla.VanillaBlockModels", remap = false)
	public static class BlockModelInjector {

		@Inject(method = "registerHeads", at = @At("TAIL"), require = 0)
		private static <BlockModelProvider> void registerHeadModel(BiConsumer<String, BlockModelProvider> models, CallbackInfo ci) {
			if (!enabled.get()) return;

			var cls = Reflection.load("net.labymod.core.client.render.blockscene.model.vanilla.VanillaBlockModels");
			var ht = Reflection.load("net.labymod.core.client.render.blockscene.model.vanilla.VanillaBlockModels$HeadType");
			var type = Enum.valueOf(Reflection.c(ht), "HUMANOID");

			// TODO use DefaultBlockSceneService#registerMdoel
			Reflection.invoke(cls, "registerHead", (BiConsumer<String, BlockModelProvider>) (a, b) -> {
				if (a.equals("minecraft:griefer_utils:head"))
					models.accept("griefer_utils:head", b);
			}, "griefer_utils:head", "", "griefer_utils:skin", type);
		}

	}

	@Pseudo
	@ExclusiveTo(LABY_4)
	@Mixin(targets = "net.labymod.core.client.render.blockscene.asset.DefaultBlockSceneAssets", remap = false)
	public static class BlockTextureInjector {

		@Redirect(method = "loadBlocking", at = @At(
			value = "INVOKE",
			target = "Ljava/util/Map;values()Ljava/util/Collection;",
			ordinal = 1
		), require = 0)
		private <SpriteSource> Collection<SpriteSource> loadBlockTexture(Map<String, SpriteSource> linkedHashMap) {
			if (!enabled.get()) return linkedHashMap.values();

			var cls = Reflection.load("net.labymod.core.client.render.blockscene.atlas.SpriteSource");

			var data = IO.read(FileProvider.getData("assets/griefer_utils/textures/skin.png")).asBytes();
			SpriteSource source = Reflection.invoke(cls, "decode", "griefer_utils:skin", data, null);

			linkedHashMap.put("griefer_utils:skin", source);
			return linkedHashMap.values();
		}

	}

	@Pseudo
	@ExclusiveTo(LABY_4)
	@Mixin(targets = "net.labymod.core.client.render.blockscene.state.BlockTable", remap = false)
	public static class BlockRegistrar {

		@ModifyArg(method = "read", at = @At(
			value = "INVOKE",
			target = "Ljava/util/Collections;unmodifiableMap(Ljava/util/Map;)Ljava/util/Map;"
		), require = 0)
		private static <Entry> Map<String, Entry> registerBlock(Map<String, Entry> blocks) {
			if (!enabled.get()) return blocks;

			blocks.put("griefer_utils:head", blocks.get("minecraft:player_head"));
			return blocks;
		}

	}

}