/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

package dev.l3g7.griefer_utils.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

/**
 * A utility class for simplified access to parts of Minecraft.
 */
public interface MinecraftUtil {

	default Minecraft mc() { return Minecraft.getMinecraft(); }
	default EntityPlayerSP player() { return mc().thePlayer; }
	default InventoryPlayer inventory() { return player().inventory; }
	default ItemStack[] armorInventory() { return inventory().armorInventory; }

	class StaticImport {

		static Minecraft mc() { return Minecraft.getMinecraft(); }
		static EntityPlayerSP player() { return mc().thePlayer; }
		static InventoryPlayer inventory() { return player().inventory; }
		static ItemStack[] armorInventory() { return inventory().armorInventory; }

	}
}
