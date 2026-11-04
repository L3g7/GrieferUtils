/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events.render;

import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import dev.l3g7.griefer_utils.core.api.event_bus.EventProfiler;
import dev.l3g7.griefer_utils.features.uncategorized.debug.DebugSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class RenderWorldLastEvent extends Event {

	public static void updateProfiler() {
		if (!DebugSettings.profileRender.get() || !DebugSettings.enabled.get()) {
			Event.setEventProfiler(null);
			return;
		}

		Event.setEventProfiler(PROFILER);
	}

	public static final EventProfiler PROFILER = new EventProfiler() {
		@Override
		public void onEventStart(Event event, boolean profilingListeners) {
			if (profilingListeners)
				mc().mcProfiler.endStartSection("griefer_utils_render_world_last");
		}

		@Override
		public void onEventEnd(Event event, boolean profilingListeners) {}

		@Override
		public boolean profileListeners(Event event) {
			return event instanceof RenderWorldLastEvent;
		}

		@Override
		public void onListenerStart(Object owner) {
			Class<?> clazz = owner instanceof Class<?> c ? c : owner.getClass();
			mc().mcProfiler.startSection(clazz.getSimpleName());
		}

		@Override
		public void onListenerEnd(Object owner) {
			mc().mcProfiler.endSection();
		}
	};

	public final float partialTicks;

	public RenderWorldLastEvent(float partialTicks) {
		this.partialTicks = partialTicks;
	}

	@Mixin(EntityRenderer.class)
	private static class MixinEntityRenderer {

		@Shadow
		private Minecraft mc;

		@Inject(method = "renderWorldPass", at = @At(value = "INVOKE_STRING", target= "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", args = "ldc=hand", shift = At.Shift.BEFORE))
	    public void injectRenderWorldPass(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
			new RenderWorldLastEvent(partialTicks).fire();
	    }

	}

}
