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

package dev.l3g7.griefer_utils.features.item;

import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

/**
 * Suppresses clicks if the durability of the held item falls below the set threshold.
 */
@Singleton
public class ToolSaver extends Feature {

	private final NumberSetting damage = new NumberSetting()
		.name("Min. Haltbarkeit")
		.icon("shield_with_sword")
		.min(1);

	private final BooleanSetting saveNonRepairable = new BooleanSetting()
		.name("Irreparables retten")
		.description("Ob Items, die nicht mehr repariert werden können, auch gerettet werden sollen.")
		.icon("broken_pickaxe")
		.defaultValue(true);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("ToolSaver")
		.description("Deaktiviert Klicks, sobald das in der Hand gehaltene Werkzeug die eingestellte Haltbarkeit unterschreitet.")
		.icon("broken_pickaxe")
		.subSettings(damage, saveNonRepairable);

	@EventListener
	public void onMouse(MouseEvent event) {
		if ((event.button != 0 && event.button != 1) || !event.buttonstate)
			return;

		if (shouldCancel())
			event.setCanceled(true);
	}

	// Required because when you break multiple blocks at once, the MouseEvent
	// is only triggered once, but the held item can be damaged multiple times
	@EventListener
	public void onTick(ClientTickEvent event) {
		if (!shouldCancel())
			return;

		KeyBinding.setKeyBindState(mc().gameSettings.keyBindUseItem.getKeyCode(), false);
		KeyBinding.setKeyBindState(mc().gameSettings.keyBindAttack.getKeyCode(), false);
	}

	private boolean shouldCancel() {
		if (player() == null)
			return false;

		ItemStack heldItem = player().getHeldItem();

		if (heldItem == null || !heldItem.isItemStackDamageable())
			return false;

		if  (!ItemUtil.canBeRepaired(heldItem) && !saveNonRepairable.get())
			return false;

		return damage.get() > heldItem.getMaxDamage() - heldItem.getItemDamage();
	}
}
