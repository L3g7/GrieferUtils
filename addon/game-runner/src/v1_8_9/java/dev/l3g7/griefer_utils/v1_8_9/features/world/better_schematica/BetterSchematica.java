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

package dev.l3g7.griefer_utils.v1_8_9.features.world.better_schematica;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.TickEvent.RenderTickEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.v1_8_9.util.SchematicaUtil;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

import java.awt.*;
import java.io.IOException;

import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.api.misc.Constants.SCHEMATICA;

@Singleton
@SuppressWarnings("unused")
public class BetterSchematica extends Feature {

	private final SwitchSetting highlightBlocks = SwitchSetting.create()
		.name("Ausgewählten Block hervorheben")
		.description("Markiert alle Blöcke vom selben Typ des in der Hand gehaltenen Items.")
		.icon("litematica/green_highlight");

	private final SwitchSetting savePosition = SwitchSetting.create()
		.name("\"Speichern\" Knopf")
		.description("Fügt in der Schematic-Kontrolle einen Knopf hinzu, der die derzeit geladene Schematic mit Drehung, Spiegelung und Position speichert."
			+ "\nWenn die Schematic geladen wird, wird sie automatich an die gespeicherte Position geschoben.")
		.icon("litematica/axes");

	private final SwitchSetting openMaterialFile = SwitchSetting.create()
		.name("Material-Datei öffnen")
		.description("Öffnet die Material-Datei nach dem Speichern.")
		.icon("file");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Schematica verbessern")
		.description("Erleichtert das Arbeiten mit Schematica.")
		.icon("litematica/litematica")
		.subSettings(highlightBlocks, savePosition, openMaterialFile);

	@Override
	public void init() {
		super.init();

		if (!SCHEMATICA) {
			enabled.name("Besseres Schematica")
				.description("Verbessert Schematica.\n\n(Schematica ist nicht installiert.)")
				.set(false)
				.disable()
				.callback(b -> {
					if (b)
						enabled.set(false);
				});
			return;
		}

		highlightBlocks.callback(() -> {
			if (isEnabled())
				SchematicaUtil.refresh();
		});

		enabled.callback(() -> {
			if (highlightBlocks.get())
				SchematicaUtil.refresh();
		});

		getCategory().callback(() -> {
			if (enabled.get() && highlightBlocks.get())
				SchematicaUtil.refresh();
		});
	}

	@EventListener
	public void onRenderTick(RenderTickEvent event) {
		if (SCHEMATICA && highlightBlocks.get())
			HighlightSchematicaBlocks.onRenderTick(event);
	}

	@EventListener
	public void onPacketSend(PacketSendEvent<C08PacketPlayerBlockPlacement> event) {
		if (SCHEMATICA && highlightBlocks.get())
			HighlightSchematicaBlocks.onPacketSend(event);
	}

	static boolean isHighlightBlocksEnabled() {
		if (!SCHEMATICA)
			return false;

		BetterSchematica betterSchematica = FileProvider.getSingleton(BetterSchematica.class);
		return betterSchematica.isEnabled() && betterSchematica.highlightBlocks.get();
	}

	static boolean isSavePositionEnabled() {
		if (!SCHEMATICA)
			return false;

		BetterSchematica betterSchematica = FileProvider.getSingleton(BetterSchematica.class);
		return betterSchematica.isEnabled() && betterSchematica.savePosition.get();
	}

	public static void openMaterialFile() {
		if (!SCHEMATICA)
			return;

		BetterSchematica betterSchematica = FileProvider.getSingleton(BetterSchematica.class);
		if (!betterSchematica.openMaterialFile.get())
			return;

		try {
			Desktop.getDesktop().open(SchematicaUtil.MATERIAL_FILE);
		} catch (IOException e) {
			labyBridge.notifyMildError("Datei konnte nicht geöffnet werden");
		}
	}

	public static void writeErrorMessage() {
		if (!SCHEMATICA)
			return;

		BetterSchematica betterSchematica = FileProvider.getSingleton(BetterSchematica.class);
		if (betterSchematica.openMaterialFile.get())
			labyBridge.notifyError("Datei konnte nicht gespeichert werden");
	}

}
