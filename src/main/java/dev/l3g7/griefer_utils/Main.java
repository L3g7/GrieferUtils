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

import dev.l3g7.griefer_utils.event.EventHandler;
import dev.l3g7.griefer_utils.event.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.settings.MainPage;
import net.labymod.api.LabyModAddon;
import net.labymod.core.asm.LabyModCoreMod;
import net.labymod.settings.elements.SettingsElement;

import java.util.List;

import static dev.l3g7.griefer_utils.util.reflection.Reflection.invoke;

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

		EventHandler.init();

		// Call all methods annotated with @OnEnable
		FileProvider.getAnnotatedMethods(OnEnable.class)
			.forEach(m -> invoke(m.owner.load(), m.load()));
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
