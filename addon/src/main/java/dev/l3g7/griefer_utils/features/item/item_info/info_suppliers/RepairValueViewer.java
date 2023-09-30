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
import dev.l3g7.griefer_utils.settings.elements.StringSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.labymod.utils.Material;
import net.labymod.utils.ModColor;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.List;

@Singleton
public class RepairValueViewer extends ItemInfo.ItemInfoSupplier {

	private final StringSetting format = new StringSetting()
		.name("Format")
		.description("Zeigt unter einem Item seinen Reparaturwert (wie viele XP-Level eine Reparatur mindestens kostet) an.")
		.icon(Material.EMPTY_MAP)
		.defaultValue("\\n&7Reparaturwert: %s")
		.setValidator(v -> {
			try {
				String.format(v, "");
				return v.contains("%s");
			} catch (IllegalFormatException e) {
				return false;
			}
		});

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Reparaturwert anzeigen")
		.description("Zeigt unter einem Item seinen Reparaturwert (wie viele XP-Level eine Reparatur mindestens kostet) an.")
		.icon(Material.ANVIL)
		.subSettings(format);

	@Override
	public List<String> getToolTip(ItemStack itemStack) {
		int cost = itemStack.getRepairCost();

		String text = String.format(ModColor.createColors(format.get()), "§r§" + getColor(itemStack) + cost + "§r");
		List<String> lines = Arrays.asList(text.split("\\\\n"));
		lines.replaceAll(s -> "§r" + s);
		return lines;
	}

	/**
	 *  0   - Green
	 * 1-39 - Yellow
	 * ≥ 40 - Red
	 */
	private char getColor(ItemStack itemStack) {
		if (itemStack.getRepairCost() < 1)
			return 'a';

		return ItemUtil.canBeRepaired(itemStack) ? 'e' : 'c';
	}

}
