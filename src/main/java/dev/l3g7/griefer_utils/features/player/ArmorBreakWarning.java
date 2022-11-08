/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

package dev.l3g7.griefer_utils.features.player;

import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.RenderWorldEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import dev.l3g7.griefer_utils.util.render.RenderUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;
import static net.labymod.utils.Material.DIAMOND_CHESTPLATE;

/**
 * Displays a warning when equipped armor falls below the set durability.
 * (0 to disable)
 */
@Singleton
public class ArmorBreakWarning extends Feature {

	private final FontRenderer font = mc().fontRendererObj;

	private final Item[] warnItems = new Item[]{Items.iron_boots, Items.iron_leggings, Items.iron_chestplate, Items.iron_helmet};
	private ItemStack currentWarnItem = null;


	@MainElement
	private final NumberSetting threshold = new NumberSetting()
		.name("ArmorBreakWarning")
		.description("Zeigt eine Warnung an, sobald eine angezogene Rüstung die eingestellte Haltbarkeit unterschreitet.", "(0 zum Deaktivieren)")
		.icon(DIAMOND_CHESTPLATE);


	@EventListener
	public void onPlayerTick(PlayerTickEvent event) {
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
