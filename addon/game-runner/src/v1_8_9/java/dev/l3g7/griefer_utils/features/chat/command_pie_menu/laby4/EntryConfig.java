/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.command_pie_menu.laby4;

import dev.l3g7.griefer_utils.core.api.misc.Citybuild;
import dev.l3g7.griefer_utils.labymod.laby4.settings.types.CitybuildSettingImpl;
import dev.l3g7.griefer_utils.labymod.laby4.settings.types.StringSettingImpl;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.CitybuildSetting;
import dev.l3g7.griefer_utils.core.settings.types.StringSetting;
import net.labymod.api.client.component.Component;
import net.labymod.api.configuration.loader.annotation.SpriteTexture;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.type.list.ListSettingConfig;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class EntryConfig extends net.labymod.api.configuration.loader.Config implements ListSettingConfig, Page.Entry {

	public final StringSettingImpl name = (StringSettingImpl) StringSetting.create()
		.name("Name")
		.description("Wie der Eintrag heißen soll.")
		.icon(Items.writable_book)
		.callback(CommandPieMenu.pages::notifyChange);

	public final StringSettingImpl command = (StringSettingImpl) StringSetting.create()
		.name("Befehl")
		.description("Welcher Befehl ausgeführt werden soll, wenn dieser Eintrag ausgewählt wird.")
		.icon(Blocks.command_block)
		.callback(CommandPieMenu.pages::notifyChange);

	public final CitybuildSettingImpl citybuild = (CitybuildSettingImpl) CitybuildSetting.create()
		.name("Citybuild")
		.description("Auf welchem Citybuild dieser Eintrag angezeigt werden soll")
		.callback(CommandPieMenu.pages::notifyChange);

	public void create(BaseSetting<?> parent) {
		name.create(parent);
		command.create(parent);
		citybuild.create(parent);
	}

	@Override
	public boolean isInvalid() {
		return name.get().isBlank() || command.get().isBlank();
	}

	@Override
	public @NotNull Component entryDisplayName() {
		return Component.text(name.get());
	}

	@Override
	public @NotNull Component newEntryTitle() {
		return Component.text("Neuer Eintrag");
	}

	@Override
	public @NotNull List<Setting> toSettings(@Nullable Setting parent, SpriteTexture texture) {
		return Arrays.asList(name, command, citybuild);
	}

	@Override
	public String name() {
		return name.get();
	}

	@Override
	public String command() {
		return command.get(); // NOTE: merge with Laby 3?
	}

	@Override
	public Citybuild citybuild() {
		return citybuild.get();
	}

}
