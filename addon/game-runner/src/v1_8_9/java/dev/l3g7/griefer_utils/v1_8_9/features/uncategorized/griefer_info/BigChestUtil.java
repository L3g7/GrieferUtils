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

package dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.griefer_info;

import dev.l3g7.griefer_utils.api.misc.Citybuild;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class BigChestUtil {

	public static Map<Integer, ItemStack> getCBs() {
		Map<Integer, ItemStack> cbs = new HashMap<>();

		for (int i = 0; i < 25; i++) {
			ItemStack itemStack = ItemUtil.CB_ITEMS.get(i + 1).copy();
			itemStack.setStackDisplayName("§6§n" + (itemStack.getDisplayName().equals("CBE") ? "CB Evil" : itemStack.getDisplayName()));
			if (i < 22)
				itemStack.stackSize = i + 1;

			cbs.put(toSlotId(i, false), itemStack);
		}

		return cbs;
	}

	public static int toSlotId(int id, boolean fullGui) {
		int rowSize = fullGui ? 7 : 5;

		int row = 1 + id / rowSize;
		return row * 9 + (fullGui ? 1 : 3) + id % rowSize;
	}

	public static char toAbbreviation(Citybuild citybuild) {
		String abbreviationString = MinecraftUtil.getCitybuildAbbreviation(citybuild.getDisplayName());

		try { // Ensure natural order is kept
			return (char) Integer.parseInt(abbreviationString);
		} catch (NumberFormatException e) {
			return abbreviationString.charAt(0);
		}
	}

}
