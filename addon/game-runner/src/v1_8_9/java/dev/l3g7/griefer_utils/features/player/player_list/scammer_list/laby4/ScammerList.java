/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player.player_list.scammer_list.laby4;

import com.google.gson.*;
import dev.l3g7.griefer_utils.core.api.BugReporter;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Icons;
import dev.l3g7.griefer_utils.labymod.laby4.temp.TempSettingActivityInitEvent;
import dev.l3g7.griefer_utils.labymod.laby4.settings.SettingsImpl;
import dev.l3g7.griefer_utils.labymod.laby4.settings.types.StringSettingImpl;
import dev.l3g7.griefer_utils.core.settings.types.StringSetting;
import dev.l3g7.griefer_utils.features.player.player_list.PlayerList;
import dev.l3g7.griefer_utils.features.player.player_list.TempScammerListBridge;
import dev.l3g7.griefer_utils.core.misc.gui.elements.ImageSelection.FileSelectionDialog;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.SettingWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static net.minecraft.util.EnumChatFormatting.RED;

@Bridge
@Singleton
@ExclusiveTo(LABY_4)
public class ScammerList extends PlayerList implements TempScammerListBridge {

	private String previousText = "";
	private boolean starting = true;

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
				labyBridge.notifyError("Ungültiger Dateipfad");
			}
		});

	public ScammerList() {
		super("§zLokale Scammerliste", "Markiert lokal hinzugefügte Scammer.", "⚠", "red_scroll", "Scammer", RED, 14, "§c§lScammer", null);
	}

	@Override
	public void init() {
		super.init();
		fileSelection.config(getConfigKey() + ".file");
		starting = false;
		fileSelection.create(fileSelection);
		fileSelection.moveCursorToEnd();
		getMainElement().addSetting(getMainElement().getChildSettings().size() - 1, fileSelection);
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

			if (!starting) // Don't show success message on startup
				labyBridge.notify("§aDatei geladen", "§aDatei konnte erfolgreich geladen werden.");
		} catch (UnsupportedOperationException | IllegalStateException | NullPointerException | JsonSyntaxException e) {
			labyBridge.notifyError("Ist der " + (starting ? "Scammer-" : "") + "Dateiinhalt richtig?");
		} catch (Throwable e) {
			labyBridge.notifyError((starting ? "Scammer-" : "") + "Datei konnte nicht geladen werden.");
		}
	}

	@Override
	public MarkAction getChatAction() {
		return chatAction.get();
	}

	private static class FileStringSetting extends StringSettingImpl {

		public FileStringSetting() {
			EventRegisterer.register(this);
		}

		@Override
		public boolean hasAdvancedButton() {
			return true;
		}

		@EventListener
		private void onInit(TempSettingActivityInitEvent event) {
			if (event.holder() != parent)
				return;

			for (Widget w : event.settings().getChildren()) {
				if (w instanceof SettingWidget s && s.setting() == this) {
					SettingsImpl.hookChildAdd(s, e -> {
						if (e.childWidget() instanceof FlexibleContentWidget content) {
							ButtonWidget btn = ButtonWidget.icon(Icons.of("explorer"), () -> {
								FileSelectionDialog.chooseFile(f -> {
									if (f == null)
										return;

									try {
										URL url = f.toURI().toURL();
										set(url.toString());
										moveCursorToEnd();
									} catch (MalformedURLException ex) {
										BugReporter.reportError(ex);
										labyBridge.notifyError("Datei konnte nicht geladen werden - WTF?");
									}
								}, "JSON-Datei", "json");
							});

							btn.addId("advanced-button"); // required so LSS is applied
							content.removeChild("advanced-button");
							content.addContent(btn);
						}
					});
					break;
				}
			}

		}

	}

}
