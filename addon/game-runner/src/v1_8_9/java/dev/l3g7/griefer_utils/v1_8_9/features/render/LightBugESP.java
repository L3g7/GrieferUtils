/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.render;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.TickEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.render.RenderWorldLastEvent;
import dev.l3g7.griefer_utils.v1_8_9.util.render.RenderUtil;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

import java.util.Set;

import static dev.l3g7.griefer_utils.settings.types.SwitchSetting.TriggerMode.TOGGLE;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.*;
import static net.minecraft.world.EnumSkyBlock.BLOCK;
import static org.lwjgl.opengl.GL11.*;

@Singleton
public class LightBugESP extends Feature {

	private final Set<AxisAlignedBB> lightBugs = new ConcurrentSet<>();
	private int passedTicks = 0;

	private final SwitchSetting inBlocks = SwitchSetting.create()
		.name("Lichtbugs in Blöcken anzeigen")
		.description("Zeigt auch Lichtbugs, an die sich in Blöcken befinden.")
		.icon("glitch_light_bulb");

	private final NumberSetting range = NumberSetting.create()
		.name("Radius")
		.description("Der Radius um den Spieler in Blöcken, in dem nach Lichtbugs überprüft wird.")
		.defaultValue(20)
		.icon(Items.compass);

	private final NumberSetting updateDelay = NumberSetting.create()
		.name("Update-Wartezeit (Ticks)")
		.description("Wie lange zwischen den Überprüfungen nach Lichtbugs gewartet werden soll.")
		.defaultValue(5)
		.icon(Items.clock);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Lichtbugs anzeigen")
		.description("Zeigt Lichtbugs an, auch durch Wände.")
		.icon("glitch_light_bulb")
		.subSettings(inBlocks, HeaderSetting.create(), range, updateDelay)
		.addHotkeySetting("das Anzeigen der Lichtbugs", TOGGLE);

	@EventListener
	private void onTick(TickEvent.ClientTickEvent event) {
		if (player() == null)
			return;

		if (passedTicks++ % Math.max(updateDelay.get(), 1) != 0)
			return;

		lightBugs.clear();
		for (int dX = -range.get(); dX <= range.get(); dX++) {
			yLoop:
			for (int dY = -range.get(); dY <= range.get(); dY++) {
				zloop:
				for (int dZ = -range.get(); dZ <= range.get(); dZ++) {
					BlockPos pos = player().getPosition().add(dX, dY, dZ);
					if (pos.getY() < 0 || pos.getY() > 255)
						continue yLoop;

					int level = world().getLightFor(BLOCK, pos);

					Block block = world().getBlockState(pos).getBlock();
					if (block.getLightValue() == level || (!inBlocks.get() && block != Blocks.air))
						continue;

					// Check if no neighbor has a higher light level (-> the light source is there)
					for (EnumFacing value : EnumFacing.values())
						if (world().getLightFor(BLOCK, pos.add(value.getDirectionVec())) > level)
							continue zloop;

					// Check if block is visible
					if (inBlocks.get()) {
						boolean visible = false;
						for (EnumFacing value : EnumFacing.values()) {
							if (block.shouldSideBeRendered(world(), pos.offset(value), value)) {
								visible = true;
								break;
							}
						}

						if (!visible)
							continue;
					}

					lightBugs.add(new AxisAlignedBB(pos, pos.add(1, 1, 1)));
				}
			}
		}
	}

	@EventListener
	private void onRenderWorldLast(RenderWorldLastEvent event) {
		Entity viewer = mc().getRenderViewEntity();
		double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
		double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
		double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;

		WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
		wr.begin(GL_QUADS, DefaultVertexFormats.POSITION);
		wr.setTranslation(-viewerX, -viewerY, -viewerZ);

		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(1, 1, 0, 48 / 255f);
		GlStateManager.disableTexture2D();
		GlStateManager.disableDepth();

		for (AxisAlignedBB bb : lightBugs)
			RenderUtil.drawFilledBoxWhenRenderingStarted(bb, false);

		Tessellator.getInstance().draw();
		wr.setTranslation(0, 0, 0);
		GL11.glColor4f(1, 1, 1, 1);
		GlStateManager.enableDepth();
		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();
		GlStateManager.popMatrix();
	}

}
