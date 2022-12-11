package dev.l3g7.griefer_utils.features.item.item_info.info_suppliers;

import dev.l3g7.griefer_utils.features.item.item_info.ItemInfo;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagEnd;
import net.minecraft.nbt.NBTTagString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Singleton
public class ItemCounter extends ItemInfo.ItemInfoSupplier {

	private final BooleanSetting ignoreDamage = new BooleanSetting()
		.name("Schaden / Sub-IDs ignorieren")
		.description("Ignoriert den Schaden / die Sub-IDs der Items beim Zählen der Anzahl.")
		.icon("broken_pickaxe");

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Item-Zähler")
		.description("Zeigt unter einem Item an, wie viele von dem Typ in dem derzeitigen Inventar vorhanden sind.")
		.icon("spyglass")
		.subSettings(ignoreDamage);

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

	private String formatAmount(int amount, int stackSize) {
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
				&& (ignoreDamage.get() || itemStack.getItemDamage() == searchedItem.getItemDamage()))
				amount += getAmount(itemStack);
		}

		return amount;
	}

	private int getAmount(ItemStack itemStack) {
		NBTBase base = itemStack.serializeNBT().getCompoundTag("tag").getCompoundTag("display").getTagList("Lore", 8).get(0);
		if (base instanceof NBTTagEnd) // Item didn't have lore
			return itemStack.stackSize;

		String amount = ((NBTTagString) base).getString();
		return amount.startsWith("§7Anzahl: §e")
			? Integer.parseInt(amount.substring(12).replace(".", "")) * itemStack.stackSize
			: itemStack.stackSize;
	}

}
