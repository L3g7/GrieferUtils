/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.modules;

import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.features.Laby4Module;
import dev.l3g7.griefer_utils.v1_8_9.misc.gui.elements.laby_polyfills.DrawUtils;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.TextColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.init.Items;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.chunk.Chunk;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.world;

@Singleton
public class ItemFrameLimitIndicator extends Laby4Module {

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

	@Override
	public Object getValue() {
		if (player() == null || world() == null)
			return Component.empty().append(Component.text("0 / " + limit.get(), TextColor.color(applyColor.get() ? calculateColor(0) : -1)));

		Chunk chunk = world().getChunkFromChunkCoords(player().chunkCoordX, player().chunkCoordZ);
		int entities = 0;
		for (ClassInheritanceMultiMap<Entity> entityList : chunk.getEntityLists())
			for (Entity entity : entityList)
				if (entity instanceof EntityItemFrame || entity instanceof EntityPainting)
					entities++;
		return Component.empty().append(Component.text(entities + " / " + limit.get(), TextColor.color(applyColor.get() ? calculateColor(entities) : -1)));
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

		return DrawUtils.toRGB(r, g, b, 0xFF);
	}

}
