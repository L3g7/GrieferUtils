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

package dev.l3g7.griefer_utils.v1_8_9.features.item.item_saver.tool_saver.laby3;

import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import net.labymod.utils.Material;
import net.minecraft.item.ItemStack;

class ItemDisplaySetting extends ListEntrySetting {

	final String name;
	final ItemStack stack;

	ItemDisplaySetting(String name, ItemStack stack) {
		super(true, false, false, new IconData(Material.STONE) /* icon */);
		setDisplayName(name);
		this.name = name;
		this.stack = stack;
	}

	@Override
	public int getObjectWidth() {
		return 0;
	}

	public ItemStack getStack() {
		return stack;
	}

	@Override
	protected void onChange() {
		FileProvider.getSingleton(ToolSaver.class).onChange();
	}

}
