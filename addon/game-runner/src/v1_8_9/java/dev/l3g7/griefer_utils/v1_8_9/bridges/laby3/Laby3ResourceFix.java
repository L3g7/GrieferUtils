/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.bridges.laby3;

import dev.l3g7.griefer_utils.api.event.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FileResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Laby3ResourceFix {

	@OnEnable
	public static void fix() throws UnsupportedEncodingException {
		List<IResourcePack> defaultResourcePacks = Reflection.get(Minecraft.getMinecraft(), "defaultResourcePacks");

		String jarPath = Laby3ResourceFix.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		if (!jarPath.contains(".jar"))
			throw new IllegalStateException("Invalid code source location: " + jarPath);

		// Sanitize jarPath
		jarPath = jarPath.substring(5, jarPath.lastIndexOf("!")); // remove protocol and class
		jarPath = URLDecoder.decode(jarPath, "UTF-8");

		defaultResourcePacks.add(new GrieferUtilsResourcePack(new File(jarPath)));
	}

	public static class GrieferUtilsResourcePack extends FileResourcePack {

		public GrieferUtilsResourcePack(File file) {
			super(file);
		}

		private ResourceLocation redirectLocation(ResourceLocation location) {
			if (location.getResourceDomain().equals("minecraft") && location.getResourcePath().startsWith("griefer_utils/"))
				return new ResourceLocation("griefer_utils", location.getResourcePath().substring("griefer_utils/".length()));

			return location;
		}

		@Override
		protected InputStream getInputStreamByName(String name) throws IOException {
			if (name.equals("pack.mcmeta"))
				return new ByteArrayInputStream("{\"pack\":{\"pack_format\":1,\"description\":\"\"}}\n".getBytes());

			return super.getInputStreamByName(name);
		}

		@Override
		public Set<String> getResourceDomains() {
			return new HashSet<>(Arrays.asList("griefer_utils", "minecraft"));
		}

		@Override
		public boolean resourceExists(ResourceLocation resourceLocation) {
			return super.resourceExists(redirectLocation(resourceLocation));
		}

		@Override
		public InputStream getInputStream(ResourceLocation resourceLocation) throws IOException {
			return super.getInputStream(redirectLocation(resourceLocation));
		}

	}

}
