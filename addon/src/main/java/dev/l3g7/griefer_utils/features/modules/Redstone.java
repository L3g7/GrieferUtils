/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

package dev.l3g7.griefer_utils.features.modules;

import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.network.MysteryModPayloadEvent;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.utils.Material;

@Singleton
public class Redstone extends Module {

	private int redstoneState = -1;

	public Redstone() {
		super("Redstone", "Zeigt dir den Redstonestatus an.", "redstone", new IconData(Material.REDSTONE));
	}

	@EventListener
	public void onMMCustomPayload(MysteryModPayloadEvent event) {
		if (!event.channel.equals("redstone"))
			return;

		redstoneState = event.payload.getAsJsonObject().get("status").getAsInt();
	}

	@Override
	public String[] getValues() {
		if (mc.theWorld == null)
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