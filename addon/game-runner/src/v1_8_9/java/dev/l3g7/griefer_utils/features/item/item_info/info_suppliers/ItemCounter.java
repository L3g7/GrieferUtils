/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.item_info.info_suppliers;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.events.ItemTooltipEvent;
import dev.l3g7.griefer_utils.core.misc.gui.guis.GuiBigChest;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.widgets.other.BlockInfo;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static net.minecraft.enchantment.EnchantmentHelper.getEnchantments;

@Singleton
public class ItemCounter extends Feature {

	private final DropDownSetting<FormatMode> formatting = DropDownSetting.create(FormatMode.class)
		.name("Formattierung")
		.description("In welchem Format die Anzahl angezeigt werden soll.")
		.icon("XZRF:color_palette")
		.defaultValue(FormatMode.FORMATTED);

	public static final SwitchSetting ignoreDamage = SwitchSetting.create()
		.name("Schaden / Sub-IDs ignorieren")
		.description("Ignoriert den Schaden / die Sub-IDs der Items beim Zählen der Anzahl.")
		.icon("XZRF:weakness");

	public static final SwitchSetting ignoreEnchants = SwitchSetting.create()
		.name("Verzauberungen ignorieren")
		.description("Ignoriert die Verzauberungen der Items beim Zählen der Anzahl.")
		.icon("XZRF:enchanted_book")
		.defaultValue(true);

	public static final SwitchSetting ignoreLore = SwitchSetting.create()
		.name("Beschreibungen ignorieren")
		.description("Ignoriert die Beschreibungen der Items beim Zählen der Anzahl.")
		.icon("XZRF:name_tag")
		.defaultValue(true);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Item-Zähler")
		.description("Zeigt unter einem Item an, wie viele von dem Typ in dem derzeitigen Inventar vorhanden sind.")
		.icon("XZRF:bundle")
		.subSettings(formatting, HeaderSetting.create(), ignoreDamage, ignoreEnchants, ignoreLore);

	@EventListener
	public void onTooltip(ItemTooltipEvent e) {
		if (FileProvider.getSingleton(BlockInfo.class).gettingTooltip)
			return;

		if (MinecraftUtil.mc().currentScreen instanceof GuiBigChest)
			return;

		GuiScreen screen = mc().currentScreen;
		if (!(screen instanceof GuiContainer))
			return;

		// Sort slots
		List<Slot> playerSlots = new ArrayList<>();
		List<Slot> chestSlots = new ArrayList<>();

		for (Slot slot : ((GuiContainer) screen).inventorySlots.inventorySlots) {
			if (slot.inventory instanceof InventoryPlayer)
				playerSlots.add(slot);
			else
				chestSlots.add(slot);
		}

		String containerName = null;
		if (!chestSlots.isEmpty())
			containerName = chestSlots.get(0).inventory.getDisplayName().getUnformattedText();
		if (screen instanceof GuiContainerCreative)
			containerName = I18n.format(CreativeTabs.creativeTabArray[((GuiContainerCreative) screen).getSelectedTabIndex()].getTranslatedTabLabel());

		long containerAmount = getAmountFromSlots(chestSlots, e.itemStack);
		long playerAmount = getAmountFromSlots(playerSlots, e.itemStack);

		// Don't add if the item is not compressed and the only one in the inv
		if (playerAmount + containerAmount == e.itemStack.stackSize)
			return;

		if (containerName != null && containerName.startsWith("§0Lager: §6") && containerAmount != 0) {
			containerAmount += Long.parseLong(containerName.substring("§0Lager: §6".length()).replace(".", ""));
			containerName = "Unendliches Lager";
		}

		int stackSize = e.itemStack.getMaxStackSize();
		// Add to tooltip
		e.toolTip.add("§r");
		e.toolTip.add("Insgesamt: " + getFormattedAmount(containerAmount + playerAmount, stackSize));

		if (containerAmount == 0 || playerAmount == 0)
			return;

		e.toolTip.add(String.format("├ %s§r§7: %s", containerName, getFormattedAmount(containerAmount, stackSize)));
		e.toolTip.add("└ Inventar: " + getFormattedAmount(playerAmount, stackSize));
	}

	private String getFormattedAmount(long amount, int stackSize) {
		String formatString = "";
		if (formatting.get() != FormatMode.UNFORMATTED) formatString += formatAmount(amount, stackSize);
		if (formatting.get() == FormatMode.BOTH) formatString += " / ";
		if (formatting.get() != FormatMode.FORMATTED) formatString += Constants.DECIMAL_FORMAT_98.format(amount);

		return formatString;
	}

	public static String formatAmount(long amount, int stackSize) {
		if (amount == 0)
			return "0 Stück";

		long pieces = amount % stackSize;
		long stacks = amount / stackSize % 54;
		long dks = amount / stackSize / 54;

		if (stackSize == 1) {
			pieces = stacks;
			stacks = 0;
		}

		String formattedString = "";

		if (dks != 0)
			formattedString += (dks > 1 ? dks + " DKs" : "eine DK");

		if (stacks != 0) {
			if (!formattedString.isEmpty()) formattedString += ", ";
			formattedString += (stacks > 1 ? stacks + " Stacks" : "ein Stack");
		}

		if (pieces != 0) {
			if (!formattedString.isEmpty()) formattedString += ", ";
			formattedString += (pieces > 1 ? pieces + " Stück" : "ein Stück");
		}

		return formattedString.trim();
	}

	private long getAmountFromSlots(List<Slot> items, ItemStack searchedItem) {
		return getAmount(items.stream().map(Slot::getStack).collect(Collectors.toList()), searchedItem);
	}

	public static long getAmount(List<ItemStack> items, ItemStack searchedItem) {
		long amount = 0;

		for (ItemStack stack : items) {
			if (stack == null || stack.getItem() == null)
				continue;

			if (stack.getItem().equals(searchedItem.getItem())
				&& (ignoreDamage.get() || stack.getItemDamage() == searchedItem.getItemDamage())
				&& (ignoreEnchants.get() || getEnchantments(stack).equals(getEnchantments(searchedItem)))
				&& (ignoreLore.get() || ItemUtil.getLore(stack).equals(ItemUtil.getLore(searchedItem))))
				amount += ItemUtil.getDecompressedAmount(stack);
		}

		return amount;
	}

	@SuppressWarnings("unused")
	public enum FormatMode implements Named {

		FORMATTED("Formattiert"), UNFORMATTED("Unformattiert"), BOTH("Beides");

		final String name;

		FormatMode(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

	}

}
