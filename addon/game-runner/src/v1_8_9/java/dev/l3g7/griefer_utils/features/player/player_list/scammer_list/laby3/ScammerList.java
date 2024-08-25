/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player.player_list.scammer_list.laby3;

import com.google.gson.*;
import dev.l3g7.griefer_utils.core.api.BugReporter;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.StringSetting;
import dev.l3g7.griefer_utils.labymod.laby3.settings.types.StringSettingImpl;
import dev.l3g7.griefer_utils.features.player.player_list.PlayerList;
import dev.l3g7.griefer_utils.features.player.player_list.TempScammerListBridge;
import dev.l3g7.griefer_utils.core.misc.gui.elements.ImageSelection.FileSelectionDialog;
import net.labymod.gui.elements.ModTextField;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.DrawUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;

@Bridge
@Singleton
@ExclusiveTo(LABY_3)
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
		super("§zLokale Scammerliste", "Markiert lokal hinzugefügte Scammer.", "⚠", "red_scroll", "Scammer", EnumChatFormatting.RED, 14, "§c§lScammer", null);
	}

	@Override
	public void init() {
		super.init();
		fileSelection.config(getConfigKey() + ".file");
		starting = false;
		((SettingsElement) getMainElement()).getSubSettings().getElements().add(8, (SettingsElement) fileSelection);
		((SettingsElement) getMainElement()).getSubSettings().getElements().add(8, (SettingsElement) HeaderSetting.create());

		ModTextField textField = Reflection.get(fileSelection, "textField");
		textField.setCursorPositionEnd();
		textField.setSelectionPos(Integer.MAX_VALUE);
	}

	private void load(URL url) {
		try {
			JsonArray entries = new JsonParser().parse(new InputStreamReader(url.openStream())).getAsJsonArray();
			uuids.clear();
			names.clear();
			for (JsonElement element : entries) {
				JsonObject entry = element.getAsJsonObject();
				uuids.add(UUID.fromString(entry.get("uuid").getAsString()));
				names.add(entry.get("name").getAsString());
			}

			if (!starting) // Don't show success message on Startup
				labyBridge.notify("§aDatei geladen", "§aDatei konnte erfolgreich geladen werden.");
		} catch (IllegalStateException | NullPointerException | JsonSyntaxException e) {
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

		private boolean drawing = false;
		private final GuiButton button = new GuiButton(-2, 0, 0, 23, 20, "");

		@Override
		public int getObjectWidth() {
			return drawing ? 141 : 114;
		}

		@Override
		public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
			super.mouseClicked(mouseX, mouseY, mouseButton);

			if (!button.mousePressed(mc, mouseX, mouseY))
				return;

			button.playPressSound(mc.getSoundHandler());
			FileSelectionDialog.chooseFile(f -> {
				if (f == null)
					return;

				try {
					URL url = f.toURI().toURL();
					set(url.toString());
					ModTextField textField = Reflection.get(this, "textField");
					textField.setCursorPositionEnd();
				} catch (MalformedURLException e) {
					BugReporter.reportError(e);
					labyBridge.notifyError("Datei konnte nicht geladen werden - WTF?");
				}
			}, "JSON-Datei", "json");
		}

		@Override
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
			drawing = true;
			super.draw(x, y, maxX, maxY, mouseX, mouseY);

			mouseOver = mouseX > x && mouseX < maxX && mouseY > y && mouseY < maxY;

			button.xPosition = maxX - 23 - 2;
			button.yPosition = y + 1;
			button.drawButton(mc, mouseX, mouseY);

			// Draw file icon
			DrawUtils drawUtils = LabyMod.getInstance().getDrawUtils();

			GlStateManager.enableBlend();
			GlStateManager.color(1, 1, 1);
			drawUtils.bindTexture("griefer_utils/icons/explorer.png");
			drawUtils.drawTexture(button.xPosition + 4, button.yPosition + 3, 0, 0, 256, 256, 14, 14, 2);
		}
	}

}
