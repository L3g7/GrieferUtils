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

package dev.l3g7.griefer_utils.v1_8_9.features.modules;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.network.MysteryModPayloadEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.Module;
import net.minecraft.init.Items;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

@Singleton
public class Redstone extends Module {

	private int redstoneState = -1;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Redstone")
		.description("Zeigt dir den Redstonestatus an.")
		.icon(Items.redstone);

	@EventListener(triggerWhenDisabled = true)
	public void onMMCustomPayload(MysteryModPayloadEvent event) {
		if (!event.channel.equals("redstone"))
			return;

		redstoneState = event.payload.getAsJsonObject().get("status").getAsInt();
	}

	@Override
	public String[] getValues() {
		if (mc().theWorld == null)
			return getDefaultValues();

		switch (redstoneState) {
			case -1:
				return new String[]{"Unbekannt"};
			case 0:
				return new String[]{"§aAktiviert"};
			default:
				return new String[]{"§4Deaktiviert"};
		}
	}

	@Override
	public String[] getDefaultValues() {
		return new String[]{"Unbekannt"};
	}
}