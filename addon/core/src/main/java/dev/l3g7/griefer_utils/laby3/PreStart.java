/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby3;

import com.google.gson.JsonObject;
import net.labymod.addon.AddonLoader;
import net.minecraft.launchwrapper.IClassTransformer;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

public class PreStart implements IClassTransformer {

	@SuppressWarnings("unchecked")
	public PreStart() throws NoSuchFieldException, IllegalAccessException {
		System.out.println("Laby3 Transformer loaded!");


		Field f = AddonLoader.class.getDeclaredField("mainClasses");
		f.setAccessible(true);
		Map<UUID, String> mainClasses = (Map<UUID, String>) f.get(null);

		UUID uuid = mainClasses.entrySet().stream()
			.filter(e -> e.getValue().equals("dev.l3g7.griefer_utils.laby4.Laby4Main"))
			.findAny().orElseThrow(NoSuchElementException::new).getKey();

		// Overwrite main class
		mainClasses.put(uuid, "dev.l3g7.griefer_utils.laby3.Main");

		// Overwrite version
		f = AddonLoader.class.getDeclaredField("loadedOffline");
		f.setAccessible(true);
		Map<UUID, JsonObject> loadedOffline = (Map<UUID, JsonObject>) f.get(null);
		loadedOffline.get(uuid).addProperty("version", 1);
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		return basicClass;
	}

}
