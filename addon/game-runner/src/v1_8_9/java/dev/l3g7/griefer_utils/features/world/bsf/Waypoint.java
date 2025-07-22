/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.bsf;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.TickEvent;
import dev.l3g7.griefer_utils.features.world.bsf.data.BSFSearchable;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

public class Waypoint extends TileEntityBeacon {

	private static final double MIN_DIST = 16;

	public static final Waypoint WAYPOINT = new Waypoint();
	private static final float[] colors = {1, 1, 1};
	private static final List<BeamSegment> SEGMENTS = ImmutableList.of(new WaypointSegment(colors));

	public static boolean enabled = false;
	public static int x;
	public static int z;
	public static BSFSearchable target;

	public static void setWaypoint(int x, int z, BSFSearchable target) {
		enabled = true;
		Waypoint.x = x;
		Waypoint.z = z;
		Waypoint.target = target;
		target.getColor().getRGBColorComponents(colors);
	}

	public static void disable() {
		enabled = false;
		Reflection.set(mc().ingameGUI, "recordPlayingUpFor", 20);
	}

	@Override
	public float shouldBeamRender() {
		return 1.0f;
	}

	@Override
	public List<BeamSegment> getBeamSegments() {
		return SEGMENTS;
	}

	@Override
	public World getWorld() {
		return world();
	}

	private static class WaypointSegment extends BeamSegment {

		public WaypointSegment(float[] colors) {
			super(colors);
		}

		@Override
		public int getHeight() {
			return 256;
		}
	}

	@EventListener
	private static void onTickEvent(TickEvent.ClientTickEvent event) {
		if (enabled)
			mc().ingameGUI.setRecordPlaying(target.getName().singular() + " (" + distanceToPlayer(Waypoint.x, Waypoint.z) + "m)", false);
	}

	@Mixin(RenderGlobal.class)
	private static class MixinRenderGlobal {

		@Shadow
		private boolean displayListEntitiesDirty;
		private boolean renderedWaypoint;

		@Inject(method = "renderEntities", at = @At("HEAD"))
		private void injectEntityRender(Entity entity, ICamera camera, float renderTicks, CallbackInfo ci) {
			renderedWaypoint = false;
		}

		@Inject(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/CompiledChunk;getTileEntities()Ljava/util/List;"))
		private void injectTileEntityRender(Entity entity, ICamera camera, float partialTicks, CallbackInfo ci) {
			if (!enabled || renderedWaypoint)
				return;

			renderedWaypoint = true;

			double xDist = x - TileEntityRendererDispatcher.staticPlayerX;
			double zDist = z - TileEntityRendererDispatcher.staticPlayerZ;

			double dist = Math.sqrt(xDist*xDist + zDist*zDist);
			if (dist <= MIN_DIST) {
				disable();
				return;
			}

			int maxDist = (settings().renderDistanceChunks - 1) * 16;
			if (maxDist < dist) {
				double distScale = maxDist / dist;
				xDist *= distScale;
				zDist *= distScale;
				dist = maxDist + dist % 16;
			}

			double scale = Math.max(1, dist / 32);

			GlStateManager.pushMatrix();
			GlStateManager.scale(scale, scale, scale);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 200, 200);
			GlStateManager.color(1, 1, 1, 1);
			TileEntityRendererDispatcher.instance.renderTileEntityAt(
				WAYPOINT,
				xDist / scale,
				-player().posY,
				zDist / scale,
				partialTicks, -1
			);
			GlStateManager.popMatrix();
		}

	}

}