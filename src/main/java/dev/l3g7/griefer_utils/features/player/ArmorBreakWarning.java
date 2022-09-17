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
import dev.l3g7.griefer_utils.util.RenderUtil;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static dev.l3g7.griefer_utils.util.RenderUtil.ARMOR_ICONS;
import static net.labymod.utils.Material.DIAMOND_CHESTPLATE;

@Singleton
public class ArmorBreakWarning extends Feature {

	private int warnSlot = -1;

	@MainElement
	private final NumberSetting damage = new NumberSetting()
		.name("ArmorBreakWarning")
		.description("Zeigt eine Warnung an, sobald eine angezogene Rüstung die eingestellte Haltbarkeit unterschreitet.", "(0 zum Deaktivieren)")
		.icon(DIAMOND_CHESTPLATE);

	/**
	 * TODO:
	 * RenderWorldEvent
	 * FileProvider plugability
	 * PlayerTickEvent pre checks
	 */

	@EventListener
	public void onPlayerTick(PlayerTickEvent event) {
		int slotId = 0;
		for (ItemStack stack : armorInventory()) {
			if (stack != null && stack.isItemStackDamageable() && damage.get() > stack.getMaxDamage() - stack.getItemDamage()) {
				warnSlot = slotId;
				return;
			}
			slotId++;
		}

		warnSlot = -1;
	}

	@EventListener
	public void onRenderWorld(RenderWorldEvent event) {
		if (warnSlot != -1)
			RenderUtil.renderTitle(ARMOR_ICONS[warnSlot] + " geht kaputt!", 2, 0xFF5555, true);
	}

}
