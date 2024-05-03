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

package dev.l3g7.griefer_utils.v1_8_9.features.modules.laby3.balances.inventory_value;

import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.v1_8_9.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import net.labymod.utils.Material;
import net.minecraft.item.ItemStack;

public class ItemDisplaySetting extends ListEntrySetting {

	private ItemStack stack; // lazy-loaded
	private final String stackNbt;
	public long value;

	public ItemDisplaySetting(String stackNbt, long value) {
		super(true, false, false, new IconData(Material.STONE));
		container = FileProvider.getSingleton(InventoryValue.class).rawBooleanElement;
		this.stackNbt = stackNbt;
		this.value = value;
	}

	public ItemDisplaySetting(ItemStack stack, long value) {
		this((String) null, value);

		this.stack = stack;
		// TODO: icon(stack);
		setDisplayName(stack.getDisplayName());
	}

	private void initStack() {
		if (stack == null) {
			stack = ItemUtil.fromNBT(stackNbt);
			// TODO icon(stack);
			setDisplayName(stack.getDisplayName());
		}
	}

	@Override
	public int getObjectWidth() {
		return 0;
	}

	@Override
	protected void onChange() {
		InventoryValue.onChange();
	}

	public ItemStack getStack() {
		initStack();
		return stack;
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		initStack();

		String displayName = getDisplayName();
		setDisplayName("§f");
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		setDisplayName(displayName);

		DrawUtils.drawString(displayName, x + 25, y + 2);
		DrawUtils.drawString("§o➡ " + Constants.DECIMAL_FORMAT_98.format(value) + "$", x + 25, y + 12);
	}

}
