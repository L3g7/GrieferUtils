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
import dev.l3g7.griefer_utils.util.render.RenderUtil;
import io.netty.util.internal.ConcurrentSet;
import net.labymod.utils.Material;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

import java.util.Set;

import static dev.l3g7.griefer_utils.settings.elements.TriggerModeSetting.TriggerMode.HOLD;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;
import static net.minecraft.world.EnumSkyBlock.BLOCK;
import static org.lwjgl.opengl.GL11.*;

@Singleton
public class LightBugESP extends Feature {

	private final Set<AxisAlignedBB> lightBugs = new ConcurrentSet<>();
	private int passedTicks = 0;

	private final TriggerModeSetting triggerMode = new TriggerModeSetting()
		.callback(() -> {
			if (getMainElement() != null)
				((BooleanSetting) getMainElement()).set(false);
		});

	private final BooleanSetting inBlocks = new BooleanSetting()
		.name("Lichtbugs in Blöcken anzeigen")
		.description("Zeigt auch Lichtbugs, an die sich in Blöcken befinden.")
		.icon("glitch_light_bulb");

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
		.description("Der Radius um den Spieler in Blöcken, in dem nach Lichtbugs überprüft wird.")
		.defaultValue(20)
		.icon(Material.COMPASS);

	private final NumberSetting updateDelay = new NumberSetting()
		.name("Update-Wartezeit (Ticks)")
		.description("Wie lange zwischen den Überprüfungen nach Lichtbugs gewartet werden soll.")
		.defaultValue(5)
		.icon(Material.WATCH);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Lichtbugs anzeigen")
		.description("Zeigt Lichtbugs an, auch durch Wände.")
		.icon("glitch_light_bulb")
		.subSettings(key, triggerMode, new HeaderSetting(), inBlocks, new HeaderSetting(), range, updateDelay);

	@EventListener
	private void onTick(TickEvent.ClientTickEvent event) {
		if (player() == null)
			return;

		if (passedTicks++ % Math.max(updateDelay.get(), 1) != 0)
			return;

		lightBugs.clear();
		for (int dX = -range.get(); dX <= range.get(); dX++) {
			for (int dY = -range.get(); dY <= range.get(); dY++) {
				loop:
				for (int dZ = -range.get(); dZ <= range.get(); dZ++) {
					BlockPos pos = player().getPosition().add(dX, dY, dZ);

					int level = world().getLightFor(BLOCK, pos);

					Block block = world().getBlockState(pos).getBlock();
					if (block.getLightValue() == level || (!inBlocks.get() && block != Blocks.air))
						continue;

					// Check if no neighbor has a higher light level (-> the light source is there)
					for (EnumFacing value : EnumFacing.VALUES)
						if (world().getLightFor(BLOCK, pos.add(value.getDirectionVec())) > level)
							continue loop;

					// Check if block is visible
					if (inBlocks.get()) {
						boolean visible = false;
						for (EnumFacing value : EnumFacing.VALUES) {
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
		GlStateManager.enableDepth();
		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();
		GlStateManager.popMatrix();
	}

}
