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

package dev.l3g7.griefer_utils.features.player;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.events.ItemUseEvent;
import dev.l3g7.griefer_utils.event.events.TickEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.TickScheduler;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.DropDownSetting;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;

import java.util.function.BiPredicate;

import static dev.l3g7.griefer_utils.features.player.AutoEat.PreferredFood.HIGH_SATURATION;
import static dev.l3g7.griefer_utils.features.player.AutoEat.TriggerMode.EFFICIENTLY;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;
import static net.labymod.utils.Material.COOKED_BEEF;

/**
 * Automatically eats when hungry.
 */
@Singleton
public class AutoEat extends Feature {

	private final DropDownSetting<TriggerMode> triggerMode = new DropDownSetting<>(TriggerMode.class)
		.name("Auslösung")
		.description("Wann AutoEat essen soll.", "Wenn effizient ausgewählt ist, wird gegessen, wenn kein Sättigungspunkt des Essens verschwendet wird.")
		.icon("bone_with_meat")
		.defaultValue(EFFICIENTLY);

	private final DropDownSetting<PreferredFood> preferredFood = new DropDownSetting<>(PreferredFood.class)
		.name("Bevorzugte Nahrung")
		.description("Welche Art von Nahrung bevorzugt gegessen werden soll.")
		.icon(COOKED_BEEF)
		.defaultValue(HIGH_SATURATION);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Automatisch essen")
		.description("Isst automatisch, wenn man Hunger hat.")
		.icon(COOKED_BEEF)
		.subSettings(triggerMode, preferredFood);

	private int previousHotbarSlot = -1;
	private boolean finishing = false;

	@EventListener
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (player() == null || finishing)
			return;

		// Check whether eating makes sense
		if (player().getItemInUse() != null) return;
		if (!player().canEat(false)) return;

		// Get best food
		int itemIndex = getSlotOfFood();
		if (itemIndex == -1) return;

		// Switch slot
		previousHotbarSlot = player().inventory.currentItem;
		player().inventory.currentItem = itemIndex;

		// Press useItem
		KeyBinding.setKeyBindState(mc().gameSettings.keyBindUseItem.getKeyCode(), true);
	}

	@EventListener
	public void onUseItemFinish(ItemUseEvent.Finish event) {
		if (previousHotbarSlot == -1)
			return;

		finishing = true;
		// Wait some ticks to finish eating, maybe because of NCP?
		TickScheduler.runAfterClientTicks(() -> {
			KeyBinding.setKeyBindState(settings().keyBindUseItem.getKeyCode(), false);
			int prevHotbarSlot = previousHotbarSlot;
			TickScheduler.runAfterClientTicks(() -> inventory().currentItem = prevHotbarSlot, 1);
			previousHotbarSlot = -1;
			finishing = false;
		}, 3);
	}

	/**
	 * Get hotbar slot with the best food
	 */
	private int getSlotOfFood() {
		int hunger = 20 - player().getFoodStats().getFoodLevel();
		int currentIndex = -1;
		int currentSaturation = 0;

		// Go through hotbar
		for (int i = 0; i < 9; i++) {
			ItemStack item = inventory().getStackInSlot(i);
			// Skip non-foods
			if (item == null || item.getItem() == null || !(item.getItem() instanceof ItemFood))
				continue;

			ItemFood food = (ItemFood) item.getItem();

			// Check if the food causes bad potion effects
			int potionId = Reflection.get(food, "potionId");
			if (potionId > 0 && Potion.potionTypes[potionId].isBadEffect())
				continue;

			int itemSaturation = food.getHealAmount(item);

			// Check if food found better than current food
			if (currentIndex == -1 || preferredFood.get().compare(currentSaturation, itemSaturation)) {
				// Update current best slot
				currentSaturation = itemSaturation;
				currentIndex = i;
			}
		}

		// Check trigger mode
		if (triggerMode.get() == EFFICIENTLY && hunger < currentSaturation)
			return -1;

		return currentIndex;
	}


	enum TriggerMode {

		HALF_BAR("bei halbem Hungerbalken"), EFFICIENTLY("effizient");

		final String name;
		TriggerMode(String name) {
			this.name = name;
		}
	}

	enum PreferredFood {

		HIGH_SATURATION("stark sättigend", (a, b) -> a < b), LOW_SATURATION("schwach sättigend", (a, b) -> a > b);

		final String name;
		final BiPredicate<Integer, Integer> compareFunc;

		PreferredFood(String name, BiPredicate<Integer, Integer> compareFunc) {
			this.name = name;
			this.compareFunc = compareFunc;
		}

		/**
		 * @return true if the second saturation is preferred
		 */
		public boolean compare(int first, int second) {
			return compareFunc.test(first, second);
		}
	}

}
