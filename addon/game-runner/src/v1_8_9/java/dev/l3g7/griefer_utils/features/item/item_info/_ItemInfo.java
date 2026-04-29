/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.item_info;

import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.settings.GUIEntry.SwitchSettingBuilder;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature.Category;

import static dev.l3g7.griefer_utils.core.settings.types.SwitchSetting.TriggerMode.HOLD;

@Singleton
public class _ItemInfo extends SwitchSettingBuilder {

	@Override
	public SwitchSetting build(Category meta, String configKey) {
		return super.build(meta, configKey)
				.addHotkeySetting("Item-Infos", HOLD);
	}

}
