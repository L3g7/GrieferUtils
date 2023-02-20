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
import dev.l3g7.griefer_utils.features.item.item_info.ItemInfo;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.labymod.utils.Material;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagEnd;
import net.minecraft.nbt.NBTTagString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static net.minecraft.enchantment.EnchantmentHelper.getEnchantments;

@Singleton
public class ItemCounter extends ItemInfo.ItemInfoSupplier {

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

	private final BooleanSetting adventureTools = new BooleanSetting()
		.name("Fehlende Adventure-Items")
		.description("Zeigt unter Adventure-Items an, wie viel noch fehlt.")
		.icon(ItemUtil.createItem(Items.diamond_shovel, 0, true))
		.defaultValue(false);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Item-Zähler")
		.description("Zeigt unter einem Item an, wie viele von dem Typ in dem derzeitigen Inventar vorhanden sind.")
		.icon("spyglass")
		.subSettings(ignoreDamage, ignoreEnchants, ignoreLore, new HeaderSetting(), adventureTools);

	@Override
	public List<String> getToolTip(ItemStack itemStack) {
		GuiScreen screen = mc().currentScreen;
		if (!(screen instanceof GuiContainer))
			return Collections.emptyList();

		if (adventureTools.get()) {
			try {
				List<String> adventureToolTip = checkForAdventure(itemStack);
				if (adventureToolTip != null)
					return adventureToolTip;
			} catch (NumberFormatException nfe) {
				System.out.println(nfe.getMessage());
			}
		}

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

		int containerAmount = getAmount(chestSlots, itemStack);
		int playerAmount = getAmount(playerSlots, itemStack);

		// Don't add if the item is not compressed and the only one in the inv
		if (playerAmount + containerAmount == itemStack.stackSize)
			return Collections.emptyList();

		int stackSize = itemStack.getMaxStackSize();
		// Add to tooltip
		List<String> toolTip = new ArrayList<>();

		toolTip.add("§r");
		toolTip.add("Insgesamt: " + formatAmount(containerAmount + playerAmount, stackSize));

		if (containerAmount == 0 || playerAmount == 0)
			return toolTip;

		toolTip.add(String.format("├ %s§r§7: %s", containerName, formatAmount(containerAmount, stackSize)));
		toolTip.add("└ Inventar: " + formatAmount(playerAmount, stackSize));

		return toolTip;
	}

	private List<String> checkForAdventure(ItemStack itemStack) {
		NBTTagCompound tag = itemStack.getTagCompound();

		List<String> toolTip = new ArrayList<>();
		toolTip.add("§r");

		if (tag != null && tag.hasKey("adventure")) {
			NBTTagCompound adventureTag = tag.getCompoundTag("adventure");
			int missingItems = adventureTag.getInteger("adventure.req_amount") - adventureTag.getInteger("adventure.amount");

			toolTip.add("Fehlende Items: " + formatAmount(missingItems, 64));
			return toolTip;
		}

		List<String> lore = ItemUtil.getLore(itemStack);

		if (lore.size() != 8 && lore.size() != 9)
			return null;

		if (!lore.get(0).startsWith("§7Status: "))
			return null;

		String task = lore.get(4);
		String searchedText = lore.size() == 8 ? "§7Baue mit dem Werkzeug §e" : "§7Liefere §e";

		if (!task.startsWith(searchedText))
			return null;

		String amount = task.substring(searchedText.length());
		amount = amount.substring(0, amount.indexOf('§'));
		toolTip.add("Benötigte Items: " + formatAmount(Integer.parseInt(amount), 64));
		return toolTip;
	}

	private String formatAmount(int amount, int stackSize) {
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

	private int getAmount(List<Slot> items, ItemStack searchedItem) {
		int amount = 0;

		for (Slot slot : items) {
			if (!slot.getHasStack())
				continue;

			ItemStack itemStack = slot.getStack();
			if (itemStack.getItem().equals(searchedItem.getItem())
				&& (ignoreDamage.get() || itemStack.getItemDamage() == searchedItem.getItemDamage())
				&& (ignoreEnchants.get() || getEnchantments(itemStack).equals(getEnchantments(searchedItem)))
				&& (ignoreLore.get() || ItemUtil.getLore(itemStack).equals(ItemUtil.getLore(searchedItem))))
				amount += getAmount(itemStack);
		}

		return amount;
	}

	private int getAmount(ItemStack itemStack) {
		NBTTagCompound tag = itemStack.serializeNBT().getCompoundTag("tag");
		if (!tag.hasKey("stackSize"))
			return itemStack.stackSize;

		NBTBase base = tag.getCompoundTag("display").getTagList("Lore", 8).get(0);
		if (base instanceof NBTTagEnd) // Item didn't have lore
			return itemStack.stackSize;

		String amount = ((NBTTagString) base).getString();
		return amount.startsWith("§7Anzahl: §e")
			? Integer.parseInt(amount.substring(12).replace(".", "")) * itemStack.stackSize
			: itemStack.stackSize;
	}

}
