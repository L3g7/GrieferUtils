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

package dev.l3g7.griefer_utils.v1_8_9.features.modules;

import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.features.Module;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.player;

@Singleton
public class MissingAdventurerBlocks extends Module {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Fehlende Adv. Blöcke")
		.description("Zeigt dir an, wie viele Blöcke mit dem in der Hand gehaltenen Adventure-Werkzeug noch abgebaut werden müssen.")
		.icon(Items.fire_charge);

	@Override
	public boolean isShown() {
		return super.isShown() && getMissingBlocks() != -1;
	}

	@Override
	public String[] getKeys() {
		return getDefaultKeys();
	}

	@Override
	public String[] getDefaultKeys() {
		return new String[] { "Fehlende Blöcke" };
	}

	@Override
	public String[] getValues() {
		return new String[] { Constants.DECIMAL_FORMAT_98.format(getMissingBlocks()) };
	}

	@Override
	public String[] getDefaultValues() {
		return new String[] { "0" };
	}

	private int getMissingBlocks() {
		return player() == null ? -1 : getMissingBlocks(player().getHeldItem());
	}

	public static int getMissingBlocks(ItemStack stack) {
		if (stack == null)
			return -1;

		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null || !tag.hasKey("adventure"))
			return -1;

		NBTTagCompound adventureTag = tag.getCompoundTag("adventure");
		return adventureTag.getInteger("adventure.req_amount") - adventureTag.getInteger("adventure.amount");
	}

}
