/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.recraft.laby3;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.labymod.laby3.temp.TempAddonsGuiWithCustomBackButton;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftBridge;
import dev.l3g7.griefer_utils.labymod.laby3.settings.types.SwitchSettingImpl;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

/**
 * Original version by Pleezon
 */
@Bridge
@Singleton
@ExclusiveTo(LABY_3)
public class RecraftBridgeImpl implements RecraftBridge {

	private final RecraftPieMenu pieMenu = new RecraftPieMenu();

	@Override
	public void openPieMenu(boolean animation) {
		pieMenu.open(animation, (SettingsElement) FileProvider.getSingleton(dev.l3g7.griefer_utils.features.item.recraft.Recraft.class).getMainElement());
	}

	@Override
	public void closePieMenu() {
		pieMenu.close();
	}

	@Override
	public BaseSetting<?> getPagesSetting() {
		return new EntryAddSetting("Seite hinzufügen")
			.callback(() -> {
				List<SettingsElement> settings = ((ControlElement) getMainSetting()).getSubSettings().getElements();
				long pageNumber = settings.stream().filter(s -> s instanceof RecraftPageSetting).count() + 1;
				RecraftPageSetting setting = new RecraftPageSetting("Seite " + pageNumber, new ArrayList<>());
				settings.add(settings.size() - 1, setting);
				mc().displayGuiScreen(new TempAddonsGuiWithCustomBackButton(RecraftBridgeImpl::save, setting));
			});
	}

	@Override
	public dev.l3g7.griefer_utils.features.item.recraft.RecraftRecording createEmptyRecording() {
		return new RecraftRecording();
	}

	private static BaseSetting<?> getMainSetting() {
		return FileProvider.getSingleton(dev.l3g7.griefer_utils.features.item.recraft.Recraft.class).getMainElement();
	}

	private static String getConfigKey() {
		return FileProvider.getSingleton(dev.l3g7.griefer_utils.features.item.recraft.Recraft.class).getConfigKey();
	}

	@Override
	public void init() {
		if (!Config.has(getConfigKey() + ".pages"))
			return;

		JsonArray pages = Config.get(getConfigKey() + ".pages").getAsJsonArray();

		List<SettingsElement> settings = ((SwitchSettingImpl) getMainSetting()).getSubSettings().getElements();
		for (JsonElement page : pages)
			settings.add(settings.size() - 1, RecraftPageSetting.fromJson(page.getAsJsonObject()));
	}

	public static void save() {
		JsonArray jsonPages = new JsonArray();

		List<RecraftPageSetting> pages = getSubSettingsOfType((SettingsElement) getMainSetting(), RecraftPageSetting.class);

		for (RecraftPageSetting page : pages)
			jsonPages.add(page.toJson());

		Config.set(getConfigKey() + ".pages", jsonPages);
		Config.save();
	}

	public static <T> List<T> getSubSettingsOfType(SettingsElement container, Class<T> type) {
		List<T> subSettings = new ArrayList<>();

		for (SettingsElement element : container.getSubSettings().getElements())
			if (type.isInstance(element))
				subSettings.add(type.cast(element));

		return subSettings;
	}

	public static void iterate(BiConsumer<Integer, RecraftRecording> consumer) {
		List<RecraftPageSetting> pages = getSubSettingsOfType((SettingsElement) getMainSetting(), RecraftPageSetting.class);
		for (int i = 0; i < pages.size(); i++) {
			List<RecraftRecording.RecordingDisplaySetting> recordings = RecraftBridgeImpl.getSubSettingsOfType(pages.get(i), RecraftRecording.RecordingDisplaySetting.class);

			for (int j = 0; j < recordings.size(); j++) {
				RecraftRecording.RecordingDisplaySetting recording = recordings.get(j);
				consumer.accept(i << 16 | j, recording.recording);
			}
		}
	}

}
