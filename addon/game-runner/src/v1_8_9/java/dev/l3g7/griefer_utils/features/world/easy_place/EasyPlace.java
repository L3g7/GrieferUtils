/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.easy_place;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.render.RenderWorldLastEvent;
import dev.l3g7.griefer_utils.core.settings.types.KeySetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3i;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;
import static net.minecraft.util.EnumFacing.values;

@Singleton
public class EasyPlace extends Feature {

	private static boolean enabled = false;
	private static EnumFacing selectedFacing = null;

	@MainElement
	private final KeySetting main = KeySetting.create()
		.name("Platzieren vereinfachen")
		.description("Ermöglicht das Platzieren auf nicht anvisierten Seiten.")
		.icon("easy_place_overlay")
		.pressCallback(b -> enabled = b);

	private static final SideRenderer[] sideRenderers = new SideRenderer[] {
		new SideRenderer(new Vec3i[] {new Vec3i(0, 1, 0), new Vec3i(0, 1, 1), new Vec3i(0, 0, 1)}), // X
		new SideRenderer(new Vec3i[] {new Vec3i(1, 0, 0), new Vec3i(1, 0, 1), new Vec3i(0, 0, 1)}), // Y
		new SideRenderer(new Vec3i[] {new Vec3i(1, 0, 0), new Vec3i(1, 1, 0), new Vec3i(0, 1, 0)})  // Z
	};

	public static void onBlockPlaceStart() {
		if (!enabled || selectedFacing == null)
			return;

		mc().objectMouseOver.sideHit = selectedFacing;
	}

	@EventListener
	private void onWorldRenderLast(RenderWorldLastEvent event) {
		MovingObjectPosition mop = mc().objectMouseOver;
		if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || !enabled) {
			selectedFacing = null;
			return;
		}

		EnumFacing facing = mop.sideHit;
		BlockPos pos = mop.getBlockPos();

		if (!world().getBlockState(pos).getBlock().isFullCube()) {
			selectedFacing = null;
			return;
		}

		GlStateManager.pushMatrix();


		Vec3i dirVec = facing.getDirectionVec();
		GlStateManager.translate(
			0.501 * dirVec.getX(),
			0.501 * dirVec.getY(),
			0.501 * dirVec.getZ()
		);

		selectedFacing = getSelectedFacing(facing, new double[] {
			mop.hitVec.xCoord - pos.getX(),
			mop.hitVec.yCoord - pos.getY(),
			mop.hitVec.zCoord - pos.getZ()
		});

		sideRenderers[facing.getAxis().ordinal()].render(pos, facing, selectedFacing);

		GlStateManager.popMatrix();
	}

	private static EnumFacing getSelectedFacing(EnumFacing facing, double[] hitCoordinates) {
		int biggestIndex = 0;
		double distTo0p5 = 0;

		int coordinatesInMiddle = 0;

		for (int i = 0; i < hitCoordinates.length; i++) {
			double dist = Math.abs(hitCoordinates[i] - 0.5);
			if (dist == 0.5) // Ignore facing currently on
				continue;

			if (dist < 0.2)
				coordinatesInMiddle++;

			if (distTo0p5 < dist) {
				distTo0p5 = dist;
				biggestIndex = i;
			}
		}

		if (coordinatesInMiddle == 2)
			return facing.getOpposite();

		Vec3i comparison = new Vec3i(
			biggestIndex == 0 ? Math.signum(hitCoordinates[0] - 0.5) : 0,
			biggestIndex == 1 ? Math.signum(hitCoordinates[1] - 0.5) : 0,
			biggestIndex == 2 ? Math.signum(hitCoordinates[2] - 0.5) : 0
		);

		for (EnumFacing value : values())
			if (value.getDirectionVec().equals(comparison))
				return value;

		throw new IllegalStateException("Selection extraction failed");
	}

	@Mixin(Minecraft.class)
	private static class MixinMinecraft {

	    @Inject(method = "rightClickMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/item/ItemStack;stackSize:I", opcode = Opcodes.GETFIELD, ordinal = 0))
	    private void injectRightClickMouse(CallbackInfo ci) {
			onBlockPlaceStart();
	    }

	}

}
