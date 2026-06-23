/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.injection;

import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import net.minecraft.launchwrapper.IClassTransformer;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.Config;

public class InjectorBase implements IClassTransformer {

	static Config mixinConfig;

	public static void initialize(String labymodNamespace, String refmap) {
		// Initialize Mixin
		MixinBootstrap.init();

		// Usage of deprecated API required for mixin 7.11 compatibility
		//noinspection deprecation
		mixinConfig = Config.create("griefer_utils.mixins.json", MixinEnvironment.getDefaultEnvironment());

		// Load refmap
		if (refmap != null)
			Reflection.set(mixinConfig.getConfig(), "refMapperConfig", "refmaps/" + refmap + ".json");

		// Register mixins
		Reflection.invoke(Mixins.class, "registerConfiguration", mixinConfig);
		if (labymodNamespace != null)
			mixinConfig.getConfig().decorate("labymod-namespace", labymodNamespace);

		Reflection.setMappingTarget(LabyBridge.labyBridge.activeMapping());
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (name.startsWith("com.github.lunatrius.schematica"))
			Constants.SCHEMATICA = true;
		else if (name.startsWith("de.emotechat.addon"))
			Constants.EMOTECHAT = true;

		return basicClass;
	}

}
