/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.modules;

import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.features.Module;
import dev.l3g7.griefer_utils.v1_8_9.misc.gui.elements.laby_polyfills.DrawUtils;
import net.minecraft.init.Items;

@Singleton
public class ItemFrameLimitIndicator extends Module {

	private final NumberSetting limit = NumberSetting.create()
		.name("Limit")
		.description("Wie viele Rahmen sich maximal gleichzeitig in einem Chunk befinden können.")
		.defaultValue(35)
		.icon(Items.item_frame);

	private final SwitchSetting applyColor = SwitchSetting.create()
		.name("Anzeige färben")
		.icon("labymod_3/tabping_colored")
		.description("Ob die Anzeige eingefärbt werden soll.")
		.defaultValue(true);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Rahmen im Chunk")
		.description("Zeigt an, wie viele Rahmen sich im derzeitigen Chunk befinden.")
		.icon(Items.item_frame)
		.subSettings(applyColor, limit);

	public String[] getValues() { throw new UnsupportedOperationException(); }
	public String[] getDefaultValues() { throw new UnsupportedOperationException(); }

	/*
	TODO:
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
*/
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

		return DrawUtils.toRGB(r, g, b, 0xFF);
	}

}
