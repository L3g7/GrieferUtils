/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.bridges;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnStartupComplete;
import net.labymod.ingamegui.Module;
import net.labymod.ingamegui.ModuleConfig;
import net.labymod.ingamegui.ModuleConfigElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;

/**
 * Fixes LabyMod 3 modules breaking due to their predecessors being uninstalled.
 */
@ExclusiveTo(LABY_3)
public class Laby3ModuleFix {

	@OnStartupComplete // Run fix additionally to loadConfig (Addon modules aren't loaded on bootup loadConfig call)
	public static void fixModules() {
		Map<String, ModuleConfigElement> configs = ModuleConfig.getConfig().getModules();
		List<String> loadedModules = Module.getModules().stream()
			.map(Module::getName)
			.collect(Collectors.toList());

		Queue<Module> detachedModules = new ArrayDeque<>(
			Module.getModules().stream()
				.filter(m -> m.getListedAfter() != null && !loadedModules.contains(m.getListedAfter()))
				.collect(Collectors.toList())
		);

		int detachedModuleCount = detachedModules.size();

		while (!detachedModules.isEmpty()) {
			Module module = detachedModules.poll();
			if (configs.containsKey(module.getListedAfter())) {
				// Copy config from predecessor
				ModuleConfigElement config = configs.get(module.getListedAfter());
				configs.put(module.getName(), config);

				// Reset config of missing module (so re-adding it doesn't destroy everything)
				configs.remove(module.getListedAfter());

				// Recursively fix
				if (config.getListedAfter() != null && !loadedModules.contains(config.getListedAfter()))
					detachedModules.add(module);
			} else
				module.setListedAfter(null);

			ModuleConfig.loadModule(module);
		}

		if (detachedModuleCount > 0) {
			System.out.println("GrieferUtils fixed " + detachedModuleCount + " detached modules");
			ModuleConfig.getConfigManager().save();
		}
	}


	@ExclusiveTo(LABY_3)
	@Mixin(ModuleConfig.class)
	public static class ModuleConfigMixin {

		@Inject(method = "loadConfig", at = @At("TAIL"), remap = false)
		private static void fixConfig(String profile, boolean force, CallbackInfo ci) {
			fixModules();
		}

	}

}
