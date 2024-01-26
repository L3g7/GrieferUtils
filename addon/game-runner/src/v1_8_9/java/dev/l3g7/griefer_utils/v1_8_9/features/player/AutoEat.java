/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.player;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Named;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.ItemUseEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.TickEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.v1_8_9.misc.TickScheduler;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.MovingObjectPosition;

import java.util.function.BiPredicate;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.*;
import static net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK;

/**
 * Automatically eats when hungry.
 */
@Singleton
public class AutoEat extends Feature {

	private final DropDownSetting<TriggerMode> triggerMode = DropDownSetting.create(TriggerMode.class)
		.name("Auslösung")
		.description("Wann AutoEat essen soll.", "Wenn effizient ausgewählt ist, wird gegessen, wenn kein Sättigungspunkt des Essens verschwendet wird.")
		.icon("bone_with_meat")
		.defaultValue(TriggerMode.EFFICIENTLY);

	private final DropDownSetting<PreferredFood> preferredFood = DropDownSetting.create(PreferredFood.class)
		.name("Bevorzugte Nahrung")
		.description("Welche Art von Nahrung bevorzugt gegessen werden soll.")
		.icon(Items.cooked_beef)
		.defaultValue(PreferredFood.HIGH_SATURATION);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Automatisch essen")
		.description("Isst automatisch, wenn man Hunger hat.")
		.icon(Items.cooked_beef)
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

		// Don't eat if the player is looking at a container
		MovingObjectPosition mop = mc().objectMouseOver;
		if (mop != null && mop.typeOfHit == BLOCK) {
			IBlockState state = world().getBlockState(mop.getBlockPos());
			if (state != null && state.getBlock() instanceof BlockContainer)
				return;
		}

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
		if (triggerMode.get() == TriggerMode.EFFICIENTLY && hunger < currentSaturation)
			return -1;

		return currentIndex;
	}


	enum TriggerMode implements Named {

		HALF_BAR("bei halbem Hungerbalken"), EFFICIENTLY("effizient");

		final String name;
		TriggerMode(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

	}

	enum PreferredFood implements Named {

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

		@Override
		public String getName() {
			return name;
		}

	}

}
