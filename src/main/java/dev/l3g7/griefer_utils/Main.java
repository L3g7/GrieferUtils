/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

package dev.l3g7.griefer_utils;

import dev.l3g7.griefer_utils.event.AnnotationEventHandler;
import dev.l3g7.griefer_utils.event.EventHandler;
import dev.l3g7.griefer_utils.event.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.settings.MainPage;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.api.LabyModAddon;
import net.labymod.core.asm.LabyModCoreMod;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.crash.CrashReport;

import java.util.List;

/**
 * The main class.
 */
public class Main extends LabyModAddon {

	private static Main instance;

	public static Main getInstance() {
		return instance;
	}

	public Main() {
		instance = this;
	}

	@Override
	public void onEnable() {
		if (!LabyModCoreMod.isForge())
			return;

		FileProvider.getClassesWithSuperClass(Feature.class).forEach(meta -> {
			if (meta.isAbstract())
				return;

			Feature instance = FileProvider.getSingleton(meta.load());
			instance.init();
		});

		try {
			EventHandler.init();
			AnnotationEventHandler.triggerEvent(OnEnable.class);
		} catch (Throwable t) {
			MinecraftUtil.mc().displayCrashReport(new CrashReport("GrieferUtils konnte nicht geladen werden!", t));
		}

	}

	@Override
	public void loadConfig() {}

	@Override
	protected void fillSettings(List<SettingsElement> list) {
		if (!LabyModCoreMod.isForge())
			return;

		list.addAll(MainPage.settings);
	}

}
