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

import dev.l3g7.griefer_utils.api.event.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.api.event.event_bus.Event;
import net.labymod.api.Laby;
import net.labymod.api.LabyAPI;
import net.labymod.api.addon.LoadedAddon;
import net.labymod.api.client.component.Component;
import net.labymod.api.configuration.settings.type.RootSettingRegistry;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.addon.lifecycle.AddonEnableEvent;
import net.labymod.api.event.addon.lifecycle.AddonPostEnableEvent;
import net.labymod.api.models.addon.annotation.AddonMain;

@AddonMain
public class Laby4Main {

	private static LoadedAddon addon;

	public static LoadedAddon getAddon() {
		if (addon == null)
			addon = Laby.labyAPI().addonService().getAddon(Laby4Main.class).orElseThrow();

		return addon;
	}

	private final Laby4Cfg configProvider = new Laby4Cfg();
	private final LabyAPI labyAPI = Laby.labyAPI();

	@Subscribe
	public final void onAddonLoad(AddonEnableEvent event) {
		Event.fire(OnEnable.class);
	}

	@Subscribe
	public final void onAddonInitialize(AddonPostEnableEvent event) {
		RootSettingRegistry registry = new RootSettingRegistry("griefer_utils", "settings") {
			public Component displayName() {
				return Component.text("GrieferUtils");
			}
		};

		registry.addSettings(configProvider);
		this.labyAPI.coreSettingRegistry().addSetting(registry);
	}

}
