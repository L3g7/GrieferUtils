/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.balance_info.inventory_value.laby4;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.widgets.Widget;
import net.minecraft.init.Items;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

@Singleton
@ExclusiveTo(LABY_4)
public class InventoryValue extends Widget {

	static final SwitchSetting auto = SwitchSetting.create()
		.name("Wert automatisch bestimmen")
		.description("Ob der Item-Wert automatisch bestimmt werden soll, oder ob nur Items mit einem manuell eingetragenen Wert gezählt werden sollen.")
		.defaultValue(true)
		.icon(Items.gold_ingot);

	static final ItemValueListSetting entries = new ItemValueListSetting()
		.name("Werte")
		.disableSubsettingConfig()
		.icon("coin_pile");

	@Feature.MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Inventar-Wert")
		.description("Zeigt dir an, wie viel ein Inventar wert ist.")
		.icon("chest")
		.subSettings(auto, HeaderSetting.create(), entries);

	@Override
	protected LabyWidget getLaby4() {
		return new InventoryValueWidget.InventoryValue();
	}

}
