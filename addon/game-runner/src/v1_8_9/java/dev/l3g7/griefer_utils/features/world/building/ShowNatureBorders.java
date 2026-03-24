/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.building;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Citybuild;
import dev.l3g7.griefer_utils.core.events.render.RenderWorldLastEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.core.util.render.RenderUtil;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.core.settings.types.SwitchSetting.TriggerMode.TOGGLE;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

@Singleton
public class ShowNatureBorders extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Plot-Grenzen anzeigen")
		.description("Zeigt die Plot-Grenzen auf Nature und Extreme an.")
		.icon("earth")
		.addHotkeySetting("die Plot-Grenzen", TOGGLE)
		.since("2.4-BETA-1");

	@EventListener
	public void onRender(RenderWorldLastEvent ignored) {
		Citybuild currentCB = MinecraftUtil.getCurrentCitybuild();
		if (currentCB != Citybuild.EXTREME && currentCB != Citybuild.NATURE)
			return;

		List<RenderLine> lines = new ArrayList<>();
		BlockPos playerPos = new BlockPos(player().posX, player().posY, player().posZ);
		BlockPos plotRoot = new BlockPos(
			Math.floorDiv(playerPos.getX(), 42) * 42,
			0,
			Math.floorDiv(playerPos.getZ(), 42) * 42
		);

		int xSize = plotRoot.getX() == -42 ? 41 : 42; // plots 0;X are one block shorter
		int zSize = plotRoot.getZ() == -42 ? 41 : 42; // plots X;0 are one block shorter

		// Fix positions if near a small plot
		if (playerPos.getX() < 0 && playerPos.getX() % 42 == 0)
			plotRoot = plotRoot.add(-42, 0, 0);
		if (playerPos.getZ() < 0 && playerPos.getZ() % 42 == 0)
			plotRoot = plotRoot.add(0, 0, -42);

		// Fix positions after passing a small plot
		if (plotRoot.getX() < 0)
			plotRoot = plotRoot.add(1, 0, 0);
		if (plotRoot.getZ() < 0)
			plotRoot = plotRoot.add(0, 0, 1);

		// vertical lines
		// X
		for (int i = 0; i <= xSize; i += 2) {
			if (xSize == 41 && i == 22) i--;
			lines.add(new RenderLine(i, 0));
		}
		// Z
		for (int i = 0; i < zSize; i += 2) {
			if (zSize == 41 && i == 22) i--;
			lines.add(new RenderLine(0, i));
		}

		// Horizontal lines
		for (int i = 0; i <= 256; i += 2) {
			lines.add(new RenderLine(new BlockPos(0, i, 0), new BlockPos(xSize, i, 0)));
			lines.add(new RenderLine(new BlockPos(0, i, 0), new BlockPos(0, i, zSize)));
		}

		// Render collected lines
		Vec3 center = new Vec3(xSize / 2d, 0, zSize / 2d);
		for (RenderLine line : lines) {
			RenderUtil.drawLine(plotRoot.add(line.startOffset), plotRoot.add(line.endOffset), Color.YELLOW, 1.5f);
			RenderLine rotLine = line.rotate(2, center);
			RenderUtil.drawLine(plotRoot.add(rotLine.startOffset), plotRoot.add(rotLine.endOffset), Color.YELLOW, 1.5f);
		}
	}

	/**
	 * A wrapper class holding a line to be rendered.
	 */
	private static class RenderLine {

		private final BlockPos startOffset, endOffset;

		public RenderLine(BlockPos startOffset, BlockPos endOffset) {
			this.startOffset = startOffset;
			this.endOffset = endOffset;
		}

		public RenderLine(int x, int z) {
			this.startOffset = new BlockPos(x, 0, z);
			this.endOffset = new BlockPos(x, 256, z);
		}

		public RenderLine rotate(int side, Vec3 center) {
			return new RenderLine(rotate(startOffset, side, center), rotate(endOffset, side, center));
		}

		private static final int[] SIN = new int[]{0, 1, 0, -1}; // sin(i * 90°)
		private static final int[] COS = new int[]{1, 0, -1, 0}; // cos(i * 90°)

		/**
		 * Rotates the given blockpos to the given side.
		 */
		private static BlockPos rotate(BlockPos point, int side, Vec3 center) {
			double dx = point.getX() - center.xCoord;
			double dz = point.getZ() - center.zCoord;

			return new BlockPos(dx * COS[side] - dz * SIN[side] + center.xCoord, point.getY(), dx * SIN[side] + dz * COS[side] + center.zCoord);
		}

	}

}
