/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.modules.laby4.balances.inventory_value;

import dev.l3g7.griefer_utils.v1_8_9.features.chat.Calculator;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemValue {

	private static final Pattern VALUE_PATTERN = Pattern.compile("\\b([\\d,.k]+)\\b");

	ItemStack stack;
	int value;

	ItemValue(ItemStack stack, int value) {
		this.stack = stack.copy();
		this.stack.stackSize = 1;
		this.value = value;
	}

	public boolean appliesTo(ItemStack stack) {
		if (stack == null)
			return false;

		ItemStack is = stack.copy();
		is.stackSize = 1;

		String nbt = ItemUtil.serializeNBT(is);
		return nbt.equals(ItemUtil.serializeNBT(this.stack)); // NOTE: better comparison
	}

	public static long autoDetect(ItemStack stack) {
		List<String> lore = ItemUtil.getLore(stack);
		if (lore.size() < 3)
			return 0;

		if (!lore.get(lore.size() - 1).startsWith("§7Signiert von"))
			return 0;

		for (String string : new String[]{lore.get(lore.size() - 2), stack.getDisplayName()}) {
			if (string.startsWith("§7Signiert von"))
				continue;

			string = string.toLowerCase()
				.replaceAll("§.", "")
				.replaceAll("(?<=\\d)\\.(\\d{3})", "$1")
				.replaceAll(" ?mio", "m")
				.replace("m", "kk");

			Matcher matcher = VALUE_PATTERN.matcher(string.replaceAll("§.", ""));
			if (matcher.find()) {
				String result = matcher.group(1);
				if (!matcher.find()) { // Cancel if multiple numbers are found
					try {
						double value = Calculator.calculate(result, false);
						if (Double.isNaN(value))
							return 0;

						return (long) value;
					} catch (NumberFormatException ignored) {}
				}
			}

		}

		// No value was found
		return 0;
	}

}
