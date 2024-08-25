/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.bridges;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FileResourcePack;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@ExclusiveTo(LABY_3)
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

		if (mc().getResourceManager() instanceof IReloadableResourceManager rm)
			rm.reloadResources(defaultResourcePacks);
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
