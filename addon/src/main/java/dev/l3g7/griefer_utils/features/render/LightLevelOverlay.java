/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.features.render;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.events.TickEvent;
import dev.l3g7.griefer_utils.event.events.render.RenderWorldLastEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.*;
import net.labymod.utils.Material;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static dev.l3g7.griefer_utils.settings.elements.TriggerModeSetting.TriggerMode.HOLD;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

@Singleton
public class LightLevelOverlay extends Feature {

	private final Map<BlockPos, Integer> lightPositions = new ConcurrentHashMap<>();
	private int passedTicks = 0;

	private final TriggerModeSetting triggerMode = new TriggerModeSetting()
		.callback(m -> {
			if (getMainElement() != null)
				((BooleanSetting) getMainElement()).set(false);
		});

	private final KeySetting key = new KeySetting()
		.name("Taste")
		.icon("key")
		.pressCallback(p -> {
			if (p || triggerMode.get() == HOLD) {
				BooleanSetting enabled = ((BooleanSetting) getMainElement());
				enabled.set(!enabled.get());
			}
		});

	private final NumberSetting range = new NumberSetting()
		.name("Radius")
		.description("Der Radius um den Spieler in Blöcken, in dem das Lichtlevel angezeigt wird.")
		.defaultValue(20)
		.icon(Material.COMPASS);

	private final NumberSetting updateDelay = new NumberSetting()
		.name("Update-Wartezeit (Ticks)")
		.description("Wie lange zwischen den Berechnung der Lichtlevel gewartet werden soll.")
		.defaultValue(5)
		.icon(Material.WATCH);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Lichtlevel anzeigen")
		.description("Zeigt das Lichtlevel auf Blöcken an.")
		.icon("light_bulb")
		.subSettings(key, triggerMode, new HeaderSetting(), range, updateDelay);

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

					if (!world().getBlockState(pos.down()).getBlock().isSideSolid(world(), pos.down(), EnumFacing.UP))
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
		double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
		double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;

		for (Map.Entry<BlockPos, Integer> entry : lightPositions.entrySet()) {
			GlStateManager.pushMatrix();
			GlStateManager.enableDepth();
			GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
			GlStateManager.enableTexture2D();

			double tx = entry.getKey().getX() - viewerX + 0.5;
			double ty = entry.getKey().getY() - viewerY + 0.01;
			double tz = entry.getKey().getZ() - viewerZ + 0.5;
			GlStateManager.translate(tx, ty, tz);

			FontRenderer font = mc().fontRendererObj;

			String str = String.valueOf(entry.getValue());

			GlStateManager.scale(0.0625, 0.05, 0.0625);
			GlStateManager.rotate(90, 1, 0, 0);
			GlStateManager.rotate(180 + mc().getRenderManager().playerViewY, 0, 0, 1);
			GlStateManager.translate(-(font.getStringWidth(str) - 1) / 2d, -(font.FONT_HEIGHT / 2d - 1), 0);

			int color = (17 * entry.getValue()) << 8 | 0xFF0000;
			font.drawString(str, 0, 0, color);
			GlStateManager.popMatrix();
		}
	}

}
