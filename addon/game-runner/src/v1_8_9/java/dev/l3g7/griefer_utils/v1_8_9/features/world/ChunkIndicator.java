/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.world;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.render.RenderWorldLastEvent;
import dev.l3g7.griefer_utils.v1_8_9.util.render.RenderUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.player;
import static net.minecraft.init.Blocks.stained_hardened_clay;

/**
 * Shows chunk boundaries.
 */
@Singleton
public class ChunkIndicator extends Feature {

	private final SwitchSetting yellow_lines = SwitchSetting.create()
		.name("Gelbe Linien", "(Alle 2 Blöcke)")
		.description("Ob die 2x2-Linien angezeigt werden sollen.")
		.icon(new ItemStack(stained_hardened_clay, 1, 4))
		.defaultValue(true);

	private final SwitchSetting cyan_lines = SwitchSetting.create()
		.name("Türkise Linien", "(Alle 8 Blöcke)")
		.description("Ob die 8x8-Linien angezeigt werden sollen.")
		.icon(new ItemStack(stained_hardened_clay, 1, 9))
		.defaultValue(true);

	private final SwitchSetting blue_lines = SwitchSetting.create()
		.name("Blaue Linien", "(Alle 16 Blöcke)")
		.description("Ob die 16x16-Linien angezeigt werden sollen.")
		.icon(new ItemStack(stained_hardened_clay, 1, 11))
		.defaultValue(true);

	private final SwitchSetting red_lines = SwitchSetting.create()
		.name("Rote Linien", "(Nachbar-Chunks)")
		.description("Ob die Linien zur Begrenzung der anliegenden Chunks angezeigt werden sollen.")
		.icon(new ItemStack(stained_hardened_clay, 1, 14))
		.defaultValue(true);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Chunk-Indikator")
		.description("Zeigt die Chunkgrenzen an. (Ähnlich wie F3 + G seit 1.10)")
		.icon("chunk")
		.subSettings(yellow_lines, cyan_lines, blue_lines, red_lines)
		.addHotkeySetting("die Chunk-Grenzen", null); // TODO TOGGLE

	private static final Color BLUE = new Color(0x3F3FFF);
	private static final Color CYAN = new Color(0x009B9B);

	@EventListener
	public void onRender(RenderWorldLastEvent ignored) {
		List<RenderLine> lines = new ArrayList<>();
		BlockPos chunkRoot = new BlockPos(player().chunkCoordX * 16, 0, player().chunkCoordZ * 16);

		// vertical lines of the outer chunks
		if (red_lines.get())
			for (int i = 0; i < 3; i++)
				lines.add(new RenderLine(16 * i, -16, Color.RED));

		// vertical lines of the inner chunk
		for (int i = 0; i < 16; i += 2)
			lines.add(new RenderLine(i, 0, getColor(i)));

		// Horizontal lines
		for (int i = 0; i <= 256; i += 2)
			lines.add(new RenderLine(new BlockPos(0, i, 0), new BlockPos(16, i, 0), getColor(i)));

		// Render collected lines
		for (RenderLine line : lines) {
			if (line.color == null)
				continue;

			for (int side = 0; side < 4; side++)
				RenderUtil.drawLine(chunkRoot.add(rotate(line.startOffset, side)), chunkRoot.add(rotate(line.endOffset, side)), line.color, 1.5f);
		}
	}

	private static final int[] SIN = new int[]{0, 1, 0, -1}; // sin(i * 90°)
	private static final int[] COS = new int[]{1, 0, -1, 0}; // cos(i * 90°)

	/**
	 * Rotates the given blockpos to the given side.
	 */
	private BlockPos rotate(BlockPos point, int side) {
		BlockPos center = new BlockPos(8, 0, 8);

		double dx = point.getX() - center.getX();
		double dz = point.getZ() - center.getZ();

		return new BlockPos(dx * COS[side] - dz * SIN[side] + center.getX(), point.getY(), dx * SIN[side] + dz * COS[side] + center.getZ());
	}

	/**
	 * @return the color of the line at the given offset.
	 */
	private Color getColor(int offset) {
		if (blue_lines.get() && offset % 16 == 0)
			return BLUE;
		if (cyan_lines.get() && offset % 8 == 0)
			return CYAN;
		if (yellow_lines.get() && offset % 2 == 0)
			return Color.YELLOW;
		return null;
	}


	/**
	 * A wrapper class holding a line to be rendered.
	 */
	private static class RenderLine {

		private final BlockPos startOffset, endOffset;
		private final Color color;

		public RenderLine(BlockPos startOffset, BlockPos endOffset, Color color) {
			this.startOffset = startOffset;
			this.endOffset = endOffset;
			this.color = color;
		}

		public RenderLine(int x, int z, Color color) {
			this.startOffset = new BlockPos(x, 0, z);
			this.endOffset = new BlockPos(x, 256, z);
			this.color = color;
		}

	}

}
