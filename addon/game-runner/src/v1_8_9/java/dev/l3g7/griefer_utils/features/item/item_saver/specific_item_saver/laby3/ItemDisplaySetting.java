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

package dev.l3g7.griefer_utils.features.item.item_saver.specific_item_saver.laby3;

import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.StringSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.labymod.laby3.settings.Icon;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.Arrays;

public class ItemDisplaySetting extends ListEntrySetting {

	public final StringSetting name;
	public final SwitchSetting drop;
	public final SwitchSetting extremeDrop;
	public final SwitchSetting leftclick;
	public final SwitchSetting rightclick;

	private final ItemStack stack;

	public ItemDisplaySetting(ItemStack stack) {
		super(true, true, false, new Icon.WrappedIcon(Icon.of(stack)));
		this.stack = stack;
		// icon(stack); // TODO
		setDisplayName(stack.getDisplayName());
		container = (SettingsElement) ItemSaver.enabled;

		name = StringSetting.create()
			.name("Anzeigename")
			.description("Der Anzeigename des Eintrags. Hat keinen Einfluss auf die geretten Items.")
			.defaultValue(stack.getDisplayName())
			.callback(this::setDisplayName)
			.icon(Items.writable_book);

		drop = SwitchSetting.create()
			.name("Droppen unterbinden")
			.description("Ob das Droppen dieses Items unterbunden werden soll.")
			.defaultValue(true)
			.icon(Blocks.dropper);

		extremeDrop = SwitchSetting.create()
			.name("Droppen unterbinden (extrem)")
			.description("Ob das Aufnehmen dieses Items in den Maus-Cursor unterbunden werden soll.")
			.icon("shield_with_sword")
			.defaultValue(false)
			.callback(b -> { if (b) drop.set(true); });

		drop.callback(b -> { if (!b) extremeDrop.set(false); });

		leftclick = SwitchSetting.create()
			.name("Linksklicks unterbinden")
			.description("Ob Linksklicks mit diesem Item unterbunden werden soll.")
			.defaultValue(stack.isItemStackDamageable())
			.icon(Items.diamond_sword);

		rightclick = SwitchSetting.create( )
			.name("Rechtsklicks unterbinden")
			.description("Ob Rechtsklicks mit diesem Item unterbunden werden soll.")
			.defaultValue(!stack.isItemStackDamageable())
			.icon(Items.bow);

		getSubSettings().getElements().clear();
		getSubSettings().getElements().addAll(Reflection.c(Arrays.asList(
			HeaderSetting.create("§r"),
			HeaderSetting.create("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
			HeaderSetting.create("§e§l" + getDisplayName().replaceAll("§.", "").replaceAll("[^\\w-äÄöÖüÜß ]", "")).scale(.7),
			HeaderSetting.create("§r").scale(.4).entryHeight(10)
		)));
		getSubSettings().getElements().addAll(Reflection.c(Arrays.asList(name, drop, extremeDrop, leftclick, rightclick)));
	}

	@Override
	public int getObjectWidth() {
		return 0;
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
