/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.events.GuiModifyItemsEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.labymod.utils.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

@Singleton
public class BetterJobExchange extends Feature {

	private static final String marker = "§6§6§8§d§7§d§4§b§3§2§7§a§e§8§6§r";

	private static final BooleanSetting calculateInv = new BooleanSetting()
		.name("Stacks im Inventar anzeigen")
		.description("Zeigt an, wie viele Stacks sich im Inventar befinden und für wie viel diese gekauft werden würden.")
		.icon(Material.CHEST)
		.defaultValue(true);

	private static final BooleanSetting calculateDKs = new BooleanSetting()
		.name("In DKs umrechnen")
		.description("Zeigt neben Angeboten eine in DKs umgerechnete Version an.")
		.icon(ItemUtil.createItem(Blocks.gold_block, 0, true));

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Jobbörse verbessern")
		.description("Verbessert die Jobbörse.")
		.icon(ItemUtil.createItem(Items.gold_ingot, 0, true))
		.subSettings(calculateInv, calculateDKs);

	@EventListener
	private void onGuiModify(GuiModifyItemsEvent event) {
		if (!event.getTitle().startsWith("§6Jobbörse"))
			return;

		for (int i = 0; i < 28; i++) {
			int x = i / 7 + 1;
			int y = i % 7 + 1;

			ItemStack stack = event.getItem(x * 9 + y);
			if (!ItemUtil.getLastLore(stack).startsWith("§7Rechtsklicke, um die Details zu den Aufträgen anzuzeigen."))
				return;

			modifyItemStack(stack);
		}
	}

	private static void modifyItemStack(ItemStack stack) {
		List<String> lore = ItemUtil.getLore(stack);
		String lastLine = lore.get(lore.size() - 1);
		if (lastLine.endsWith(marker))
			return;

		List<Pair<Integer, Integer>> offers = lore.subList(0, lore.size() - 3).stream().map(BetterJobExchange::extractOffer).collect(Collectors.toList());

		int money = 0;
		int ownedStacks = getOwnedStacks(stack);
		int remainingStacks = ownedStacks;

		for (int i = 0; i < offers.size(); i++) {
			Pair<Integer, Integer> offer = offers.get(i);
			int stacks = offer.getLeft();
			if (remainingStacks > 0) {
				money += Math.min(remainingStacks, stacks) * offer.getRight();
				remainingStacks -= stacks;
			}

			if (calculateDKs.get()) {
				String line = String.format("§7 / §e%s á §a%d§2$", stacks == 54 ? "1§7 DK" : String.format("%.1f§7 DKs", (stacks * 10 / 54) / 10d), offer.getRight() * 54);
				lore.set(i, lore.get(i) + line);
			}
		}

		if (remainingStacks > 0)
			ownedStacks -= remainingStacks;

		if (calculateInv.get()) {
			lore.set(lore.size() - 2, ownedStacks == 1
				? String.format("§7Klicke, um §eden Stack§7 im Inventar für §a%d§2$§7 zu liefern.", money)
				: String.format("§7Klicke, um alle §e%d§7 Stacks im Inventar für §a%d§2$§7 zu liefern.", ownedStacks, money));
		}
		lore.set(lore.size() - 1, lastLine + marker);
		ItemUtil.setLore(stack, lore);
	}

	private static Pair<Integer, Integer> extractOffer(String line) {
		line = line.substring(7);
		Integer stacks = Integer.parseInt(line.substring(0, line.indexOf('§')));

		line = line.substring(line.indexOf("§a") + 2, line.length() - 3);
		return Pair.of(stacks, Integer.parseInt(line));
	}

	private static int getOwnedStacks(ItemStack requiredStack) {
		int ownedItems = 0;

		for (ItemStack stack : player().inventory.mainInventory) {
			if (!requiredStack.isItemEqual(stack))
				continue;

			if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("stackSize")) {
				// The item isn't compressed
				if (stack.stackSize == stack.getMaxStackSize())
					ownedItems += stack.stackSize;

				continue;
			}

			ownedItems += ItemUtil.getDecompressedAmount(stack);
		}

		return ownedItems / requiredStack.getMaxStackSize();
	}

}