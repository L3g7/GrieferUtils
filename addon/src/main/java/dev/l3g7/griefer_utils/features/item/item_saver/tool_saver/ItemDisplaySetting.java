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

package dev.l3g7.griefer_utils.features.item.item_saver.tool_saver;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.settings.elements.ListEntrySetting;
import net.minecraft.item.ItemStack;

class ItemDisplaySetting extends ListEntrySetting {

	final String name;
	final ItemStack stack;

	private final IconStorage iconStorage = new IconStorage();

	ItemDisplaySetting(String name, ItemStack stack) {
		super(true, false, false);
		container = FileProvider.getSingleton(ToolSaver.class).enabled;
		name(name);
		this.name = name;
		this.stack = stack;
		icon(stack);
	}

	@Override
	public int getObjectWidth() {
		return 0;
	}

	@Override
	public IconStorage getIconStorage() {
		return iconStorage;
	}

	public ItemStack getStack() {
		return stack;
	}

	@Override
	protected void onChange() {
		FileProvider.getSingleton(ToolSaver.class).onChange();
	}

}
