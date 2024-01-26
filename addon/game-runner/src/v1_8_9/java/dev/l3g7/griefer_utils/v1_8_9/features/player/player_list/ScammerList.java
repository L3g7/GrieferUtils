/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.player.player_list;

import com.google.gson.*;
import dev.l3g7.griefer_utils.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

import static net.minecraft.util.EnumChatFormatting.RED;

@Singleton
public class ScammerList extends PlayerList {

	private String previousText = "";

	/*
	private final StringSetting fileSelection = new FileStringSetting()
		.name("Datei")
		.description("Eine lokale Datei, aus der Scammer geladen werden sollen.")
		.icon("file")
		.callback(s -> {
			if (previousText.equals(s))
				return;

			previousText = s;
			try {
				load(new URL(s));
			} catch (MalformedURLException e) {
				displayAchievement("§cFehler", "§cUngültiger Dateipfad");
			}
		});*/

	public ScammerList() {
		super("§zLokale Scammerliste", "Markiert lokal hinzugefügte Scammer.", "⚠", "red_scroll", "Scammer", RED, 14, "§c§lScammer", null);
	}

	@Override
	public void init() {
		super.init();/*
		fileSelection.config(getConfigKey() + ".file");
		getMainElement().getSubSettings().getElements().add(8, fileSelection);
		getMainElement().getSubSettings().getElements().add(8, new HeaderSetting());

		ModTextField textField = Reflection.get(fileSelection, "textField");
		textField.setCursorPositionEnd();
		textField.setSelectionPos(Integer.MAX_VALUE);*/
	}

	private void load(URL url) {
		try {
			JsonArray entries = JsonParser.parseReader(new InputStreamReader(url.openStream())).getAsJsonArray();
			uuids.clear();
			names.clear();
			for (JsonElement element : entries) {
				JsonObject entry = element.getAsJsonObject();
				uuids.add(UUID.fromString(entry.get("uuid").getAsString()));
				names.add(entry.get("name").getAsString());
			}

			LabyBridge.labyBridge.notify("§aGrieferUtils", "§aDatei konnte erfolgreich geladen werden.");
		} catch (UnsupportedOperationException | IllegalStateException | NullPointerException | JsonSyntaxException e) {
			LabyBridge.labyBridge.notifyError("Ist der Dateiinhalt richtig?");
		} catch (Throwable e) {
			LabyBridge.labyBridge.notifyError("Datei konnte nicht geladen werden.");
		}
	}

}
