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

package dev.l3g7.griefer_utils.features.item.item_info.info_suppliers;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.features.item.item_info.ItemInfo;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.DropDownSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.labymod.utils.Material;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagEnd;
import net.minecraft.nbt.NBTTagString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static net.minecraft.enchantment.EnchantmentHelper.getEnchantments;

@Singleton
public class ItemCounter extends ItemInfo.ItemInfoSupplier {

	private final DropDownSetting<FormatMode> formatting = new DropDownSetting<>(FormatMode.class)
		.name("Formattierung")
		.description("Zeigt die Anzahl unformattiert an.")
		.icon(Material.BOOK_AND_QUILL)
		.defaultValue(FormatMode.FORMATTED);

	private final BooleanSetting ignoreDamage = new BooleanSetting()
		.name("Schaden / Sub-IDs ignorieren")
		.description("Ignoriert den Schaden / die Sub-IDs der Items beim Zählen der Anzahl.")
		.icon("broken_pickaxe");

	private final BooleanSetting ignoreEnchants = new BooleanSetting()
		.name("Verzauberungen ignorieren")
		.description("Ignoriert die Verzauberungen der Items beim Zählen der Anzahl.")
		.icon(Material.ENCHANTED_BOOK)
		.defaultValue(true);

	private final BooleanSetting ignoreLore = new BooleanSetting()
		.name("Beschreibungen ignorieren")
		.description("Ignoriert die Beschreibungen der Items beim Zählen der Anzahl.")
		.icon(Material.PAPER)
		.defaultValue(true);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Item-Zähler")
		.description("Zeigt unter einem Item an, wie viele von dem Typ in dem derzeitigen Inventar vorhanden sind.")
		.icon("spyglass")
		.subSettings(formatting, new HeaderSetting(), ignoreDamage, ignoreEnchants, ignoreLore);

	@Override
	public List<String> getToolTip(ItemStack itemStack) {
		GuiScreen screen = mc().currentScreen;
		if (!(screen instanceof GuiContainer))
			return Collections.emptyList();

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

		int containerAmount = getAmountFromSlots(chestSlots, itemStack);
		int playerAmount = getAmountFromSlots(playerSlots, itemStack);

		// Don't add if the item is not compressed and the only one in the inv
		if (playerAmount + containerAmount == itemStack.stackSize)
			return Collections.emptyList();

		int stackSize = itemStack.getMaxStackSize();
		// Add to tooltip
		List<String> toolTip = new ArrayList<>();

		toolTip.add("§r");
		toolTip.add("Insgesamt: " + getFormattedAmount(containerAmount + playerAmount, stackSize));

		if (containerAmount == 0 || playerAmount == 0)
			return toolTip;

		toolTip.add(String.format("├ %s§r§7: %s", containerName, getFormattedAmount(containerAmount, stackSize)));
		toolTip.add("└ Inventar: " + getFormattedAmount(playerAmount, stackSize));

		return toolTip;
	}

	private String getFormattedAmount(int amount, int stackSize) {
		String formatString = "";
		if (formatting.get() != FormatMode.UNFORMATTED) formatString += formatAmount(amount, stackSize);
		if (formatting.get() == FormatMode.BOTH) formatString += " / ";
		if (formatting.get() != FormatMode.FORMATTED) formatString += Constants.DECIMAL_FORMAT_98.format(amount);

		return formatString;
	}

	public static String formatAmount(int amount, int stackSize) {
		if (amount == 0)
			return "0 Stück";

		int pieces = amount % stackSize;
		int stacks = amount / stackSize % 54;
		int dks = amount / stackSize / 54;

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

	private int getAmountFromSlots(List<Slot> items, ItemStack searchedItem) {
		return getAmount(items.stream().map(Slot::getStack).collect(Collectors.toList()), searchedItem);
	}

	public int getAmount(List<ItemStack> items, ItemStack searchedItem) {
		int amount = 0;

		for (ItemStack stack : items) {
			if (stack == null)
				continue;

			if (stack.getItem().equals(searchedItem.getItem())
				&& (ignoreDamage.get() || stack.getItemDamage() == searchedItem.getItemDamage())
				&& (ignoreEnchants.get() || getEnchantments(stack).equals(getEnchantments(searchedItem)))
				&& (ignoreLore.get() || ItemUtil.getLore(stack).equals(ItemUtil.getLore(searchedItem))))
				amount += getAmount(stack);
		}

		return amount;
	}

	private int getAmount(ItemStack itemStack) {
		NBTTagCompound tag = itemStack.getTagCompound();
		if (tag == null || !tag.hasKey("stackSize"))
			return itemStack.stackSize;

		NBTBase base = tag.getCompoundTag("display").getTagList("Lore", 8).get(0);
		if (base instanceof NBTTagEnd) // Item didn't have lore
			return itemStack.stackSize;

		String amount = ((NBTTagString) base).getString();
		return amount.startsWith("§7Anzahl: §e")
			? Integer.parseInt(amount.substring(12).replace(".", "")) * itemStack.stackSize
			: itemStack.stackSize;
	}

	@SuppressWarnings("unused")
	enum FormatMode {

		FORMATTED("Formattiert"), UNFORMATTED("Unformattiert"), BOTH("Beides");

		final String name;

		FormatMode(String name) {
			this.name = name;
		}
	}

}
