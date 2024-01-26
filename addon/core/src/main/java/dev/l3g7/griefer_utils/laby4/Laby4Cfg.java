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

package dev.l3g7.griefer_utils.laby4;

import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.laby4.settings.BaseSettingImpl;
import dev.l3g7.griefer_utils.settings.SettingLoader;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.annotation.SpriteTexture;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.type.SettingElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ConfigName("confignamebeans")
public class Laby4Cfg extends Config {

	@NotNull
	public List<Setting> toSettings(@Nullable Setting parent, SpriteTexture unused) {
		ArrayList<Setting> settings = new ArrayList<>();

		Object acc = FileProvider.getSingleton(Reflection.load("dev.l3g7.griefer_utils.v1_8_9.features.chat.AntiCommandChoker"));
		BaseSettingImpl<?,?> enabled = Reflection.get(acc, "enabled");
		enabled.create(this, parent);
		settings.add(enabled);
		SettingLoader.initMainElement(acc, "chat");

		settings.sort(Comparator.comparingInt(e -> ((SettingElement) e).getOrderValue()));
		return settings;
	}

}
