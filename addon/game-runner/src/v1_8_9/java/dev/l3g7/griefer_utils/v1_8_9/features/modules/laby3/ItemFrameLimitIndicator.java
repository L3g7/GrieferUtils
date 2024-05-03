/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.v1_8_9.features.modules.laby3;

import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.features.modules.Laby3Module;
import net.labymod.utils.Material;
import net.labymod.utils.ModColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.chunk.Chunk;

import java.util.Collections;
import java.util.List;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.world;

@Singleton
public class ItemFrameLimitIndicator extends Laby3Module {

	private final NumberSetting limit = NumberSetting.create()
		.name("Limit")
		.description("Wie viele Rahmen sich maximal gleichzeitig in einem Chunk befinden können.")
		.defaultValue(35)
		.icon(Material.ITEM_FRAME);

	private final SwitchSetting applyColor = SwitchSetting.create()
		.name("Anzeige färben")
		.icon("labymod_3/tabping_colored")
		.description("Ob die Anzeige eingefärbt werden soll.")
		.defaultValue(true);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Rahmen im Chunk")
		.description("Zeigt an, wie viele Rahmen sich im derzeitigen Chunk befinden.")
		.icon(Material.ITEM_FRAME)
		.subSettings(applyColor, limit);

	public String[] getValues() { throw new UnsupportedOperationException(); }
	public String[] getDefaultValues() { throw new UnsupportedOperationException(); }

	public List<List<Text>> getDefaultTextValues() {
		return Collections.singletonList(Collections.singletonList(Text.getText("0 / " + limit.get(), 0x00FF00)));
	}

	public List<List<Text>> getTextValues() {
		if (player() == null || world() == null)
			return getDefaultTextValues();

		Chunk chunk = world().getChunkFromChunkCoords(player().chunkCoordX, player().chunkCoordZ);
		int entities = 0;
		for (ClassInheritanceMultiMap<Entity> entityList : chunk.getEntityLists())
			for (Entity entity : entityList)
				if (entity instanceof EntityItemFrame || entity instanceof EntityPainting)
					entities++;

		Text text = Text.getText(entities + " / " + limit.get(), applyColor.get() ? calculateColor(entities) : -1);
		return Collections.singletonList(Collections.singletonList(text));
	}

	private int calculateColor(int entities) {
		double percent = Math.min(entities / (double) limit.get(), 1);
		int r, g, b;

		if (percent > 0.5) {
			double scalar = 2 * (1 - percent);
			r = 0xFF;
			g = (int) (0xFF * scalar);
			b = (int) (0x55 * scalar);
		} else {
			r = (int) (0x55 + 0xAA * 2 * percent);
			g = 0xFF;
			b = 0x55;
		}

		return ModColor.toRGB(r, g, b, 0xFF);
	}

}