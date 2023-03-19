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

package dev.l3g7.griefer_utils.features.world;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.item.item_info.info_suppliers.ItemCounter;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Singleton
public class BetterAdventurer extends Feature {

	private final BooleanSetting displayCount = new BooleanSetting()
		.name("Fehlende Items anzeigen")
		.description("")
		.description("Zeigt unter Adventure-Items an, wie viel noch fehlt.")
		.icon(ItemUtil.createItem(Items.diamond_shovel, 0, true))
		.defaultValue(true);

	private final BooleanSetting coinAmount = new BooleanSetting()
		.name("Coin Anzeige fixen")
		.description("Setzt die Anzahl des Coin-Anzeige-Items auf die Anzahl der Coins.")
		.icon(ItemUtil.createItem(new ItemStack(Items.fire_charge, 128, 0), true, null))
		.defaultValue(true);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Besserer Adventurer")
		.description("Verbessert den Adventurer.")
		.icon(ItemUtil.createItem(Items.fire_charge, 0, true))
		.subSettings(displayCount, coinAmount);

	@EventListener
	public void onTooltip(ItemTooltipEvent e) {
		GuiScreen screen = mc().currentScreen;
		if (!(screen instanceof GuiContainer))
			return;

		if (displayCount.get()) {
			try {
				List<String> adventureToolTip = getMissingItems(e.itemStack);
				if (adventureToolTip != null)
					e.toolTip.addAll(adventureToolTip);
			} catch (NumberFormatException nfe) {
				System.out.println(nfe.getMessage());
			}
		}
	}

	@EventListener
	public void onTick(TickEvent.RenderTickEvent event) {
		if (!(mc().currentScreen instanceof GuiChest))
			return;

		GuiChest screen = (GuiChest) mc().currentScreen;
		IInventory inv = Reflection.get(screen, "lowerChestInventory");
		boolean isAdventurer = inv.getName().startsWith("§6Adventure-Jobs");
		if (!isAdventurer && !inv.getName().startsWith("§6Shop"))
			return;

		ItemStack stack = screen.inventorySlots.getSlot(isAdventurer ? 40 : 49).getStack();
		if (stack == null || stack.getItem() != Items.fire_charge)
			return;

		if (EnchantmentHelper.getEnchantments(stack).isEmpty())
			return;

		List<String> lore = ItemUtil.getLore(stack);
		if (lore.size() < 1 || !lore.get(0).startsWith("§7Anzahl: §e"))
			return;

		try {
			String amount = lore.get(0).substring("§7Anzahl: §e".length());
			amount = amount.substring(0, amount.indexOf(isAdventurer ? '§' : ' '));
			stack.stackSize = Integer.parseInt(amount);
		} catch (NumberFormatException | StringIndexOutOfBoundsException e) {
			System.err.println(lore.get(0));
		}
	}

	private List<String> getMissingItems(ItemStack itemStack) {
		NBTTagCompound tag = itemStack.getTagCompound();

		List<String> toolTip = new ArrayList<>();
		toolTip.add("§r");

		if (tag != null && tag.hasKey("adventure")) {
			NBTTagCompound adventureTag = tag.getCompoundTag("adventure");
			int missingItems = adventureTag.getInteger("adventure.req_amount") - adventureTag.getInteger("adventure.amount");

			toolTip.add("Fehlende Items: " + ItemCounter.formatAmount(missingItems, 64));
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
		toolTip.add("Benötigte Items: " + ItemCounter.formatAmount(Integer.parseInt(amount), 64));
		return toolTip;
	}
}
