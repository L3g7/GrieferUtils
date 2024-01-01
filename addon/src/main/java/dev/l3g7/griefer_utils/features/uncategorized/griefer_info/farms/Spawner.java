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

package dev.l3g7.griefer_utils.features.uncategorized.griefer_info.farms;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class Spawner {

	static Spawner fromJson(JsonObject object) {
		SpawnerType spawnerType = SpawnerType.TYPES_BY_ID.get(object.get("entity").getAsString());
		if (spawnerType == null)
			System.err.println("Missing spawner type: " + object.get("entity").getAsString());

		int amount = object.get("spawner").getAsInt();
		boolean active = object.get("spawntype").getAsString().equals("Aktiv");
		String plot = object.get("plot").getAsString();

		return new Spawner(spawnerType, amount, active, plot);
	}

	public final SpawnerType type;
	public int amount;
	public final boolean active;
	public final String plot;

	private Spawner(SpawnerType type, int amount, boolean active, String plot) {
		this.type = type;
		this.amount = amount;
		this.active = active;
		this.plot = plot;
	}

	public ItemStack toStack(String farmName) {
		ItemStack stack = type.isCobblestone()
			? ItemUtil.createItem(new ItemStack(Blocks.cobblestone, amount), false, "§6§nBruchstein")
			: new ItemStack(Blocks.command_block, amount).setStackDisplayName("§6§n" + type.germanName);

		stack.setStackDisplayName(stack.getDisplayName() + "§7 [" + (active ? "A" : "P") + "]");
		ItemUtil.setLore(stack, "§8/p h " + (plot.isEmpty() ? farmName : plot));

		return stack;
	}

	@Override
	public String toString() {
		return String.format("Spawner{type='%s',passive=%b,amount=%d,plot='%s',hash=%d}", type.germanName, !active, Integer.MAX_VALUE - amount, plot, hashCode());
	}

}
