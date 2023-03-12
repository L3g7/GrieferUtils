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

package dev.l3g7.griefer_utils.features.uncategorized;

import dev.l3g7.griefer_utils.event.EventListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

/**
 * Reverts the damage to MoosLeitung's BirthKlinge lost by a bug in ItemSaver.
 * This is an easter egg :D
 */
public class BirthDamageFaker {

	private static final boolean active = System.getProperty("fixMoosleitungBirthKlinge") != null;

	@EventListener
	private static void onRender(TickEvent.RenderTickEvent event) {
		if (!active)
			return;

		if (player() == null)
			return;

		for (ItemStack itemStack : player().inventory.mainInventory) {
			if (itemStack == null)
				return;

			NBTTagCompound tag = itemStack.getTagCompound();
			if (tag != null && tag.hasKey("display", 10)) {
				NBTTagCompound nbttagcompound = tag.getCompoundTag("display");

				if (nbttagcompound.hasKey("Lore", 9)) {
					NBTTagList list = nbttagcompound.getTagList("Lore", 8);
					if (list.tagCount() >= 2) {
						NBTTagString str = (NBTTagString) list.get(1);
						if (str.getString().equals("§6Edle Klinge von §c§lM§6§lo§e§lo§a§ls§b§lL§d§le§c§li§6§lt§e§lu§a§ln§b§lg"))
							itemStack.setItemDamage(itemStack.getMaxDamage());
					}
				}
			}
		}
	}

}