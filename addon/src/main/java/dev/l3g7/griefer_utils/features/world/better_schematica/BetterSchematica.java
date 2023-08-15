/*
 * This file is part of GrieferUtils https://github.com/L3g7/GrieferUtils.
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 the "License";
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

package dev.l3g7.griefer_utils.features.world.better_schematica;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.event.events.TickEvent;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.SchematicaUtil;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

@Singleton
public class BetterSchematica extends Feature {

	private final BooleanSetting highlightBlocks = new BooleanSetting()
		.name("Ausgewählten Block hervorheben")
		.description("Markiert alle Blöcke vom selben Typ des in der Hand gehaltenen Items.")
		.icon("litematica/green_highlight");

	private final BooleanSetting savePosition = new BooleanSetting()
		.name("\"Speichern\" Knopf")
		.description("Fügt in der Schematic-Kontrolle einen Knopf hinzu, der die derzeit geladene Schematic mit Drehung, Spiegelung und Position speichert."
			+ "\nWenn die Schematic geladen wird, wird sie automatich an die gespeicherte Position geschoben.")
		.icon("litematica/axes");

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Schematica verbessern")
		.description("Erleichtert das Arbeiten mit Schematica.")
		.icon("litematica/litematica")
		.subSettings(highlightBlocks, savePosition);

	@Override
	public void init() {
		super.init();

		if (Constants.SCHEMATICA) {
			highlightBlocks.callback(b -> {
				if (isEnabled())
					SchematicaUtil.refresh();
			});

			enabled.callback(b -> {
				if (highlightBlocks.get())
					SchematicaUtil.refresh();
			});

			getCategory().getSetting().addCallback(b -> {
				if (enabled.get() && highlightBlocks.get())
					SchematicaUtil.refresh();
			});
			return;
		}

		enabled.name("§8Besseres Schematica")
			.description("Verbessert Schematica.\n\n(Es ist kein Schematica installiert.)")
			.set(false)
			.callback(b -> {
				if (b)
					enabled.set(false);
			});
	}

	@EventListener
	public void onRenderTick(TickEvent.RenderTickEvent event) {
		if (Constants.SCHEMATICA && highlightBlocks.get())
			HighlightSchematicaBlocks.onRenderTick(event);
	}

	@EventListener
	public void onPacketSend(PacketEvent.PacketSendEvent<C08PacketPlayerBlockPlacement> event) {
		if (Constants.SCHEMATICA && highlightBlocks.get())
			HighlightSchematicaBlocks.onPacketSend(event);
	}

	static boolean isHighlightBlocksEnabled() {
		if (!Constants.SCHEMATICA)
			return false;

		BetterSchematica betterSchematica = FileProvider.getSingleton(BetterSchematica.class);
		return betterSchematica.isEnabled() && betterSchematica.highlightBlocks.get();
	}

	static boolean isSavePositionEnabled() {
		if (!Constants.SCHEMATICA)
			return false;

		BetterSchematica betterSchematica = FileProvider.getSingleton(BetterSchematica.class);
		return betterSchematica.isEnabled() && betterSchematica.savePosition.get();
	}

}
