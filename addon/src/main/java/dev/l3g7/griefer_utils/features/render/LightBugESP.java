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

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.RenderWorldLastEvent;
import dev.l3g7.griefer_utils.event.events.TickEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.*;
import dev.l3g7.griefer_utils.util.render.RenderUtil;
import io.netty.util.internal.ConcurrentSet;
import net.labymod.utils.Material;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Set;

import static dev.l3g7.griefer_utils.settings.elements.TriggerModeSetting.TriggerMode.HOLD;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.world;
import static net.minecraft.world.EnumSkyBlock.BLOCK;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;

@Singleton
public class LightBugESP extends Feature {

	private final Set<BlockPos> lightBugs = new ConcurrentSet<>();
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
		.subSettings(key, triggerMode, new HeaderSetting(), range, updateDelay);

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

					// Check if no neighbor has a higher light level (-> the light source is there)
					for (EnumFacing value : EnumFacing.VALUES)
						if (world().getLightFor(BLOCK, pos.add(value.getDirectionVec())) > level)
							continue loop;

					if (world().getBlockState(pos).getBlock().getLightValue() != level)
						lightBugs.add(pos);
				}
			}
		}
	}

	@EventListener
	private void onRenderWorldLast(RenderWorldLastEvent event) {
		GL11.glDisable(GL_DEPTH_TEST);
		for (BlockPos pos : lightBugs) {
			AxisAlignedBB bb = new AxisAlignedBB(pos, pos.add(1, 1, 1));
			RenderUtil.drawFilledBox(bb, new Color(0x30FFFF00, true), false);
			RenderUtil.drawBoxOutlines(bb, new Color(0xFFFF00), 2f);
		}
		GL11.glEnable(GL_DEPTH_TEST);
	}

}
