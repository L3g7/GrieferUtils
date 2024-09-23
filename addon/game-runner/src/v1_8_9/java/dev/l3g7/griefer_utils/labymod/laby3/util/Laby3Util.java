/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.util;

import com.mojang.authlib.GameProfile;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import net.labymod.ingamechat.tabs.GuiChatNameHistory;
import net.labymod.main.LabyMod;
import net.labymod.utils.texture.DynamicModTexture;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class Laby3Util { // FIXME: dissolve; moving these methods to their usage results in crashes due to wrong mappings

	public static DynamicModTexture createDynamicTexture(String path, String url) {
		return new DynamicModTexture(new ResourceLocation(path), url);
	}

	public static Pair<String, String> getCachedTexture(UUID uuid) {
		ResourceLocation resourceSkin = LabyMod.getInstance().getDrawUtils().getPlayerSkinTextureCache().getSkinTexture(new GameProfile(uuid, ""));
		if (resourceSkin == null)
			return null;

		return new Pair<>(resourceSkin.getResourceDomain(), resourceSkin.getResourcePath());
	}

	public static void openNameHistory(String name) {
		if (name.startsWith("!")) {
			labyBridge.notify("§eUngültiger Name", "§fVon Bedrock-Spielern kann kein Namensverlauf abgefragt werden.");
			return;
		}

		mc().displayGuiScreen(new GuiChatNameHistory("", name));
	}

}
