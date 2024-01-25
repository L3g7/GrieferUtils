/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.better_schematica;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.events.TickEvent;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.SchematicaUtil;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

import java.awt.*;
import java.io.IOException;

import static dev.l3g7.griefer_utils.core.misc.Constants.SCHEMATICA;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.displayAchievement;

@Singleton
@SuppressWarnings("unused")
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

	private final BooleanSetting openMaterialFile = new BooleanSetting()
		.name("Material-Datei öffnen")
		.description("Öffnet die Material-Datei nach dem Speichern.")
		.icon("file");

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Schematica verbessern")
		.description("Erleichtert das Arbeiten mit Schematica.")
		.icon("litematica/litematica")
		.subSettings(highlightBlocks, savePosition, openMaterialFile);

	@Override
	public void init() {
		super.init();

		if (!SCHEMATICA) {
			enabled.name("§8Besseres Schematica")
				.description("Verbessert Schematica.\n\n(Es ist kein Schematica installiert.)")
				.set(false)
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

		getCategory().getSetting().callback(() -> {
			if (enabled.get() && highlightBlocks.get())
				SchematicaUtil.refresh();
		});
	}

	@EventListener
	public void onRenderTick(TickEvent.RenderTickEvent event) {
		if (SCHEMATICA && highlightBlocks.get())
			HighlightSchematicaBlocks.onRenderTick(event);
	}

	@EventListener
	public void onPacketSend(PacketEvent.PacketSendEvent<C08PacketPlayerBlockPlacement> event) {
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
			displayAchievement("§e§lFehler \u26A0", "Datei konnte nicht geöffnet werden");
		}
	}

	public static void writeErrorMessage() {
		if (!SCHEMATICA)
			return;

		BetterSchematica betterSchematica = FileProvider.getSingleton(BetterSchematica.class);
		if (betterSchematica.openMaterialFile.get())
			displayAchievement("§c§lFehler \u26A0", "§cDatei konnte nicht gespeichert werden");
	}

}
