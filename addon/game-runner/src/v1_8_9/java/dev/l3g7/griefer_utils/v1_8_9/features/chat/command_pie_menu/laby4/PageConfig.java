/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.chat.command_pie_menu.laby4;

import dev.l3g7.griefer_utils.laby4.settings.types.StringSettingImpl;
import dev.l3g7.griefer_utils.settings.BaseSetting;
import dev.l3g7.griefer_utils.settings.types.StringSetting;
import net.labymod.api.client.component.Component;
import net.labymod.api.configuration.loader.annotation.SpriteTexture;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.type.list.ListSettingConfig;
import net.minecraft.init.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;

public class PageConfig extends net.labymod.api.configuration.loader.Config implements ListSettingConfig, Page {

	public final StringSettingImpl name = (StringSettingImpl) StringSetting.create()
		.name("Name")
		.icon(Items.writable_book)
		.callback(CommandPieMenu.pages::notifyChange);

	public final EntryListSetting entries = new EntryListSetting()
		.name("Einträge")
		.icon("command_pie_menu")
		.callback(CommandPieMenu.pages::notifyChange);

	public PageConfig(String name) {
		this.name.set(name);
	}

	public void create(BaseSetting<?> parent) {
		name.create(parent);
		entries.create(parent);
	}

	@Override
	public boolean isInvalid() {
		return name.get().isBlank();
	}

	@Override
	public @NotNull Component entryDisplayName() {
		return Component.text(name.get());
	}

	@Override
	public @NotNull List<Setting> toSettings(@Nullable Setting parent, SpriteTexture texture) {
		return Arrays.asList(name, entries);
	}

	@Override
	public String name() {
		return name.get();
	}

	@Override
	public List<Entry> entries() {
		return c(entries.get());
	}

}
