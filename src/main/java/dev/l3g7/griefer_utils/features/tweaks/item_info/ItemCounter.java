package dev.l3g7.griefer_utils.features.tweaks.item_info;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.features.tweaks.item_info.ItemInfo.ItemInfoSupplier;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Singleton
public class ItemCounter extends ItemInfoSupplier {

	private static final List<String> levels = ImmutableList.of("ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN");

	private final BooleanSetting enabled = new BooleanSetting()
			.name("Item-Zähler")
			.description("Zeigt unter einem Item an, wie viele von dem Typ in dem derzeitigen Inventar vorhanden sind.")
			.config("tweaks.item_info.zip_preview.active")
			.icon("spyglass")
			.defaultValue(false);

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	@Override
	public List<String> getToolTip(ItemStack itemStack) {
		if (!isActive() || !isOnGrieferGames() || !(mc().currentScreen instanceof GuiContainer))
			return Collections.emptyList();

		// Sort slots
		List<Slot> playerSlots = new ArrayList<>();
		List<Slot> chestSlots = new ArrayList<>();

		for (Slot slot : ((GuiContainer) mc().currentScreen).inventorySlots.inventorySlots) {
			if (slot.inventory instanceof InventoryPlayer)
				playerSlots.add(slot);
			else
				chestSlots.add(slot);
		}

		String containerName = null;
		if (!chestSlots.isEmpty())
			containerName = chestSlots.get(0).inventory.getDisplayName().getUnformattedText();

		// Convert to ints
		Item item = itemStack.getItem();
		int containerAmount = getAmount(chestSlots, item);
		int playerAmount = getAmount(playerSlots, item);

		// Don't add if the item is not compressed and the only one in the inv
		if (playerAmount + containerAmount == itemStack.stackSize)
			return Collections.emptyList();

		// Add to tooltip
		List<String> toolTip = new ArrayList<>();

		toolTip.add("§r");
		toolTip.add("Insgesamt: " + formatAmount(containerAmount + playerAmount));

		if (containerAmount == 0 || playerAmount == 0)
			return toolTip;

		toolTip.add(String.format("├ %s§r§7: %s", containerName, formatAmount(containerAmount)));
		toolTip.add("└ Inventar: " + formatAmount(playerAmount));

		return toolTip;
	}

	private String formatAmount(int amount) {
		int pieces = amount % 64;
		int stacks = amount / 64 % 54;
		int dks = amount / 64 / 54;

		String formattedString = "";

		if (dks != 0)
			formattedString += (dks > 1 ? dks + " DKs" : "eine DK");

		if (stacks != 0) {
			if (!formattedString.isEmpty()) formattedString += ", ";
			formattedString += (stacks > 1 ? stacks + " Stacks" : "ein Stack");
		}

		if (pieces != 0) {
			if (!formattedString.isEmpty()) formattedString += ", ";
			formattedString += pieces + " Stück";
		}

		return formattedString.trim();
	}

	private int getAmount(List<Slot> items, Item searchedItem) {
		int amount = 0;

		for (Slot slot : items) {
			if (!slot.getHasStack())
				continue;

			ItemStack itemStack = slot.getStack();
			if (itemStack.getItem().equals(searchedItem))
				amount += getAmount(itemStack);
		}

		return amount;
	}

	private int getAmount(ItemStack itemStack) {
		return Math.max(levels.indexOf(itemStack.serializeNBT().getCompoundTag("tag").getString("compressionLevel")), 1) * itemStack.stackSize;
	}
}