/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.world;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.GuiModifyItemsEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.player;

@Singleton
public class BetterJobExchange extends Feature {

	private static final String marker = "§6§6§8§d§7§d§4§b§3§2§7§a§e§8§6§r";

	private static final SwitchSetting calculateInv = SwitchSetting.create()
		.name("Stacks im Inventar anzeigen")
		.description("Zeigt an, wie viele Stacks sich im Inventar befinden und für wie viel diese gekauft werden würden.")
		.icon(Blocks.chest)
		.defaultValue(true);

	private static final SwitchSetting calculateDKs = SwitchSetting.create()
		.name("In DKs umrechnen")
		.description("Zeigt neben Angeboten eine in DKs umgerechnete Version an.")
		.icon(ItemUtil.createItem(Blocks.gold_block, 0, true));

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
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
			int endIndex = ItemUtil.getLore(stack).indexOf("§7Rechtsklicke, um die Details zu den Aufträgen anzuzeigen.");
			if (endIndex == -1)
				continue;

			modifyItemStack(stack, endIndex);
		}
	}

	private static void modifyItemStack(ItemStack stack, int endIndex) {
		List<String> lore = ItemUtil.getLore(stack);
		String lastLine = lore.get(lore.size() - 1);
		if (lastLine.endsWith(marker))
			return;

		List<Pair<Integer, Integer>> offers = lore.subList(0, endIndex - 2).stream().map(BetterJobExchange::extractOffer).collect(Collectors.toList());

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
