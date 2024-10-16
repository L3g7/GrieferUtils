/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.render;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.core.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.TickEvent;
import dev.l3g7.griefer_utils.core.events.render.RenderWorldLastEvent;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static dev.l3g7.griefer_utils.core.settings.types.SwitchSetting.TriggerMode.TOGGLE;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

@Singleton
public class LightLevelOverlay extends Feature {

	private final Map<BlockPos, Integer> lightPositions = new ConcurrentHashMap<>();
	private final ResourceLocation texture = new ResourceLocation("griefer_utils", "textures/light_level_overlay.png");
	private final double[] textureX = new double[17];
	private final double[] textureY = new double[9];
	private int passedTicks = 0;

	private final NumberSetting range = NumberSetting.create()
		.name("Radius")
		.description("Der Radius um den Spieler in Blöcken, in dem das Lichtlevel angezeigt wird.")
		.defaultValue(20)
		.icon(Items.compass);

	private final NumberSetting updateDelay = NumberSetting.create()
		.name("Update-Wartezeit (Ticks)")
		.description("Wie lange zwischen den Berechnungen der Lichtlevel gewartet werden soll.")
		.defaultValue(5)
		.icon(Items.clock);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Lichtlevel anzeigen")
		.description("Zeigt das Lichtlevel auf Blöcken an.")
		.icon("light_bulb")
		.subSettings(range, updateDelay)
		.addHotkeySetting("das Anzeigen des Lichtlevels", TOGGLE);

	public LightLevelOverlay() {
		for (int i = 0; i <= 16; i++)
			textureX[i] = i / 16d;

		for (int i = 0; i <= 8; i++)
			textureY[i] = i / 8d;
	}

	@EventListener
	private void onTick(TickEvent.ClientTickEvent event) {
		if (world() == null)
			return;

		if (passedTicks++ % Math.max(updateDelay.get(), 1) != 0)
			return;

		lightPositions.clear();

		for (int dX = -range.get(); dX <= range.get(); dX++) {
			for (int dY = -range.get(); dY <= range.get(); dY++) {
				for (int dZ = -range.get(); dZ <= range.get(); dZ++) {
					BlockPos pos = player().getPosition().add(dX, dY, dZ);
					if (world().getBlockState(pos).getBlock() != Blocks.air)
						continue;

					Block downBlock = world().getBlockState(pos.down()).getBlock();
					if (!isUpperSideSolid(downBlock, world(), pos.down()) && !(downBlock instanceof BlockFarmland))
						continue;

					lightPositions.put(pos, world().getLightFor(EnumSkyBlock.BLOCK, pos));
				}
			}
		}
	}

	@EventListener
	private void onRenderWorldLast(RenderWorldLastEvent event) {
		Entity viewer = mc().getRenderViewEntity();
		double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
		double viewerY = viewer.lastTickPosY - 0.01 + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
		double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;

		GlStateManager.pushMatrix();
		DrawUtils.bindTexture(texture);
		WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
		wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		wr.setTranslation(-viewerX, -viewerY, -viewerZ);

		double rotation = viewer.rotationYaw % 360;
		if (rotation < 0)
			rotation += 360;

		int rot = (int) (Math.round(rotation / 45d) % 8);

		for (Map.Entry<BlockPos, Integer> entry : lightPositions.entrySet()) {
			BlockPos p = entry.getKey();
			wr.pos(p.getX()    , p.getY(), p.getZ()    ).tex(textureX[entry.getValue()    ], textureY[rot    ]).color(255, 255, 255, 255).endVertex();
			wr.pos(p.getX()    , p.getY(), p.getZ() + 1).tex(textureX[entry.getValue()    ], textureY[rot + 1]).color(255, 255, 255, 255).endVertex();
			wr.pos(p.getX() + 1, p.getY(), p.getZ() + 1).tex(textureX[entry.getValue() + 1], textureY[rot + 1]).color(255, 255, 255, 255).endVertex();
			wr.pos(p.getX() + 1, p.getY(), p.getZ()    ).tex(textureX[entry.getValue() + 1], textureY[rot    ]).color(255, 255, 255, 255).endVertex();
		}

		Tessellator.getInstance().draw();
		wr.setTranslation(0, 0, 0);
		GlStateManager.popMatrix();
	}

	private boolean isUpperSideSolid(Block block, IBlockAccess world, BlockPos pos) {
		if (block instanceof BlockFarmland)
			return false;

		if (block instanceof BlockHopper || block instanceof BlockCompressedPowered)
			return true;

		IBlockState state = block.getActualState(world.getBlockState(pos), world, pos);

		if (block instanceof BlockSnow)
			return state.getValue(BlockSnow.LAYERS) >= 8;

		if (block instanceof BlockStairs)
			return state.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.TOP;

		if (block instanceof BlockSlab)
			return block.isFullBlock() || (state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP);

		return block.isNormalCube();
	}

}
