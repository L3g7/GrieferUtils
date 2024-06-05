/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.item_saver;

import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.core.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.core.events.TickEvent;
import dev.l3g7.griefer_utils.core.events.render.RenderWorldEvent;
import dev.l3g7.griefer_utils.features.item.item_saver.ItemSaverCategory.ItemSaver;
import dev.l3g7.griefer_utils.core.util.render.RenderUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

/**
 * Displays a warning when equipped armor falls below the set durability.
 * (0 to disable)
 */
@Singleton
public class ArmorBreakWarning extends ItemSaver {

	private final FontRenderer font = mc().fontRendererObj;

	private final Item[] warnItems = new Item[]{Items.iron_boots, Items.iron_leggings, Items.iron_chestplate, Items.iron_helmet};
	private ItemStack currentWarnItem = null;


	@MainElement
	private final NumberSetting threshold = NumberSetting.create()
		.name("Bei Rüstungsschaden warnen")
		.description("Zeigt eine Warnung an, sobald eine angezogene Rüstung die eingestellte Haltbarkeit unterschreitet.", "(0 zum Deaktivieren)")
		.icon(Items.diamond_chestplate);

	@EventListener
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (player() == null)
			return;

		currentWarnItem = null;

		// Go through armor
		ItemStack[] armor = armorInventory();
		for (int i = 0; i < armor.length; i++) {
			ItemStack stack = armor[i];

			// Check if item can get damaged
			if (stack == null || !stack.isItemStackDamageable())
				continue;

			int itemDurability = stack.getMaxDamage() - stack.getItemDamage();

			// Check if durability is less than threshold
			if (threshold.get() > itemDurability)
				currentWarnItem = new ItemStack(warnItems[i]);
		}
	}


	@EventListener
	public void onRenderWorld(RenderWorldEvent event) {
		ItemStack warnItem = this.currentWarnItem; // Avoid concurrent modifications
		if (warnItem == null)
			return;

		float scale = 2;

		int strWidth = font.getStringWidth("§cgeht kaputt!") + 18; // 16px Item + 2px padding
		float x = (screenWidth() - strWidth * scale) / 2f;
		float y = screenHeight() / 2f + 3; // 3px shift

		GlStateManager.scale(scale, scale, 0);
		GlStateManager.translate(x / scale, y / scale, 0);

		RenderUtil.renderItem(warnItem, 0, 0, 0xFFFF5555);
		font.drawStringWithShadow("§cgeht kaputt!", 18, 4, -1);
	}

}
