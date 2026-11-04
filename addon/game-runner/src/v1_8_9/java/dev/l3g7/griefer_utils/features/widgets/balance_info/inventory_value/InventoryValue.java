/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.balance_info.inventory_value;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.events.WindowClickEvent;
import dev.l3g7.griefer_utils.core.misc.gui.guis.GuiBigChest;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.settings.types.list.ListSetting;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.widgets.Laby3Widget;
import dev.l3g7.griefer_utils.features.widgets.Laby4Widget;
import dev.l3g7.griefer_utils.features.widgets.Widget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.settings.LabyModModuleEditorGui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;
import static net.labymod.api.client.gui.hud.hudwidget.text.TextLine.State.HIDDEN;
import static net.labymod.api.client.gui.hud.hudwidget.text.TextLine.State.VISIBLE;

@Singleton
public class InventoryValue extends Widget {

	private final SwitchSetting auto = SwitchSetting.create()
		.name("Wert automatisch bestimmen")
		.description("Ob der Item-Wert automatisch bestimmt werden soll, oder ob nur Items mit einem manuell eingetragenen Wert gezählt werden sollen.")
		.defaultValue(true)
		.icon("gold_ingot");

	private final ListSetting<ItemValue> entries = ListSetting.create(ItemValue.class)
		.name("Werte")
		.icon("coin")
		.customAdd(this::addProtectedItem);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Inventar-Wert")
		.description("Zeigt dir an, wie viel ein Inventar wert ist.")
		.icon("bundle")
		.subSettings(auto, entries);

	@Override
	protected LabyWidget getLaby3() {
		return new InventoryValueL3();
	}

	@Override
	protected LabyWidget getLaby4() {
		return new InventoryValueL4();
	}

	// Item add logic

	/**
	 * The screen that triggered the current item add selection.
	 * If null, no selection is active.
	 */
	private GuiScreen previousScreen = null;

	private void addProtectedItem() {
		if (mc().thePlayer == null) {
			LabyBridge.labyBridge.notify("§e§lFehler ⚠", "§eHinzufügen von Items ist nur ingame möglich!");
			return;
		}

		previousScreen = mc().currentScreen;
		display(Constants.ADDON_PREFIX + "Bitte klicke das Item an, das du hinzufügen möchtest.");
		MinecraftUtil.closeClientsideGUI();
	}

	@EventListener(triggerWhenDisabled = true)
	private void onAddItem(WindowClickEvent event) {
		if (previousScreen == null || event.itemStack == null)
			return;

		mc().displayGuiScreen(previousScreen);
		entries.add(new ItemValue(event.itemStack));
		entries.notifyChange();
		previousScreen = null;
		event.cancel();
	}

	// Helpers

	private String getValue(List<ItemStack> itemStacks) {
		long total = 0;

		itemLoop:
		for (ItemStack stack : itemStacks) {
			if (stack == null)
				continue;

			for (ItemValue value : entries.get()) {
				if (value.appliesTo(stack)) {
					total += (long) value.value.get() * stack.stackSize;
					continue itemLoop;
				}
			}

			if (auto.get())
				total += ItemValue.autoDetect(stack) * stack.stackSize;
		}

		return Constants.DECIMAL_FORMAT_98.format(total) + "$";
	}

	@ExclusiveTo(LABY_3)
	private class InventoryValueL3 extends Laby3Widget {

		@Override
		public String[] getKeys() {
			if (mc.currentScreen instanceof GuiInventory
				|| mc.currentScreen instanceof GuiBigChest
				|| !(mc.currentScreen instanceof GuiContainer)
				&& !(mc.currentScreen instanceof LabyModModuleEditorGui))
				return new String[]{"Eigenes Inventar"};

			return getDefaultKeys();
		}

		@Override
		public String[] getDefaultKeys() {
			return new String[]{"Eigenes Inventar", "Geöffnetes Inventar"};
		}

		@Override
		public String[] getValues() {
			if (player() == null)
				return getDefaultValues();

			// Check if an inventory is open
			String ownInventory = InventoryValue.this.getValue(Arrays.asList(player().inventory.mainInventory));

			GuiScreen screen = mc.currentScreen;
			if (!(screen instanceof GuiContainer) || screen instanceof GuiInventory || screen instanceof GuiBigChest)
				return mc.currentScreen instanceof LabyModModuleEditorGui ? new String[]{ownInventory, "0$"} : new String[]{ownInventory};

			// Update openInventory
			List<Slot> slots = ((GuiContainer) mc.currentScreen).inventorySlots.inventorySlots;
			slots = slots.subList(0, slots.size() - 9 * 4);
			String openInventory = InventoryValue.this.getValue(slots.stream().map(Slot::getStack).collect(Collectors.toList()));

			return new String[]{ownInventory, openInventory};
		}

		@Override
		public String[] getDefaultValues() {
			return new String[]{"0$", "0$"};
		}

	}

	@ExclusiveTo(LABY_4)
	private class InventoryValueL4 extends Laby4Widget {

		private TextLine ownInventory, openInventory;

		@Override
		protected void createText() {
			ownInventory = createLine("Eigenes Inventar", "0$");
			openInventory = createLine("Geöffnetes Inventar", "0$");
		}

		@Override
		public void onTick(boolean isEditorContext) {
			if (player() == null) {
				ownInventory.updateAndFlush("0$");
				openInventory.updateAndFlush("0$");
				return;
			}

			ownInventory.updateAndFlush(InventoryValue.this.getValue(Arrays.asList(player().inventory.mainInventory)));

			// Check if an inventory is open
			GuiScreen screen = mc().currentScreen;
			if (player() == null || !(screen instanceof GuiContainer) || screen instanceof GuiInventory || screen instanceof GuiBigChest) {
				openInventory.setState(HIDDEN);
				openInventory.updateAndFlush("0$");
				return;
			}

			// Update openInventory
			List<Slot> slots = ((GuiContainer) mc().currentScreen).inventorySlots.inventorySlots;
			slots = slots.subList(0, slots.size() - 9 * 4);

			openInventory.setState(VISIBLE);
			openInventory.updateAndFlush(InventoryValue.this.getValue(slots.stream().map(Slot::getStack).collect(Collectors.toList())));
		}

	}

}
