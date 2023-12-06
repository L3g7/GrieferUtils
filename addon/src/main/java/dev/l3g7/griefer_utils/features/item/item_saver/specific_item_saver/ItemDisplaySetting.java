/*
 * This file is part of GrieferUtils https://github.com/L3g7/GrieferUtils.
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 the "License";
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

package dev.l3g7.griefer_utils.features.item.item_saver.specific_item_saver;

import dev.l3g7.griefer_utils.misc.gui.guis.AddonsGuiWithCustomBackButton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.ListEntrySetting;
import dev.l3g7.griefer_utils.settings.elements.StringSetting;
import net.labymod.utils.Material;
import net.minecraft.item.ItemStack;

public class ItemDisplaySetting extends ListEntrySetting {

	public final StringSetting name;
	public final BooleanSetting drop;
	public final BooleanSetting extremeDrop;
	public final BooleanSetting leftclick;
	public final BooleanSetting rightclick;

	private final IconStorage iconStorage = new IconStorage();
	private final ItemStack stack;

	public ItemDisplaySetting(ItemStack stack) {
		super(true, true, false);
		container = ItemSaver.enabled;
		this.stack = stack;
		icon(stack);
		name(stack.getDisplayName());

		name = new StringSetting()
			.name("Anzeigename")
			.description("Der Anzeigename des Eintrags. Hat keinen Einfluss auf die geretten Items.")
			.defaultValue(stack.getDisplayName())
			.callback(s -> name(s))
			.icon(Material.BOOK_AND_QUILL);

		drop = new BooleanSetting()
			.name("Droppen unterbinden")
			.description("Ob das Droppen dieses Items unterbunden werden soll.")
			.defaultValue(true)
			.icon(Material.DROPPER);

		extremeDrop = new BooleanSetting()
			.name("Droppen unterbinden (extrem)")
			.description("Ob das Aufnehmen dieses Items in den Maus-Cursor unterbunden werden soll.")
			.icon("shield_with_sword")
			.defaultValue(false)
			.callback(b -> { if (b) drop.set(true); });

		drop.callback(b -> { if (!b) extremeDrop.set(false); });

		leftclick = new BooleanSetting()
			.name("Linksklicks unterbinden")
			.description("Ob Linksklicks mit diesem Item unterbunden werden soll.")
			.defaultValue(stack.isItemStackDamageable())
			.icon(Material.DIAMOND_SWORD);

		rightclick = new BooleanSetting()
			.name("Rechtsklicks unterbinden")
			.description("Ob Rechtsklicks mit diesem Item unterbunden werden soll.")
			.defaultValue(!stack.isItemStackDamageable())
			.icon(Material.BOW);

		subSettings(name, drop, extremeDrop, leftclick, rightclick);
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
	protected void openSettings() {
		mc.displayGuiScreen(new AddonsGuiWithCustomBackButton(ItemSaver::onChange, this));
	}

	@Override
	protected void onChange() {
		ItemSaver.onChange();
	}

}
