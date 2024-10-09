/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.labymod.user.UserManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;

@Singleton
@ExclusiveTo(LABY_3)
public class RemoveWalkingMiniMes extends Feature {

	public static final int WALKING_MINIME_ID = 1476;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Walking Minimes entfernen")
		.description("Entfernt alle \"Walking Minimes\", da diese in Labymod 3 Lags und Crashes verursachen können.")
		.icon("crossed_out_walking_minime");

	 @Mixin(value = UserManager.class, remap = false)
	 private static class MixinUserManager {

	     @ModifyVariable(method = "handleJsonString", at = @At(value = "STORE", ordinal = 0))
	     private JsonArray modifyCosmeticData(JsonArray instance) {
			 if (!FileProvider.getSingleton(RemoveWalkingMiniMes.class).isEnabled())
				 return instance;

			 JsonArray copy = new JsonArray();
		     for (JsonElement element : instance) {
			     JsonObject data = element.getAsJsonObject();
			     if (!data.has("i") || data.get("i").getAsInt() != WALKING_MINIME_ID)
				     copy.add(element);
		     }

		     return copy;
	     }

	 }

}
