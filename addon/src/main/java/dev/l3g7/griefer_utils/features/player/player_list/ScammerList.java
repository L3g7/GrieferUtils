package dev.l3g7.griefer_utils.features.player.player_list;

import com.google.gson.*;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.FileSelection;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.features.uncategorized.settings.BugReporter;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.StringSetting;
import net.labymod.gui.elements.ModTextField;
import net.labymod.main.LabyMod;
import net.labymod.utils.DrawUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.displayAchievement;
import static net.labymod.utils.ModColor.RED;

@Singleton
public class ScammerList extends PlayerList {

	private String previousText = "";

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
		});

	public ScammerList() {
		super("§zLokale Scammer", "Markiert lokal hinzugefügte Scammer.", "⚠", "red_scroll", "Scammer", RED, 14, null);
	}

	@Override
	public void init() {
		super.init();
		fileSelection.config(getConfigKey() + ".file");
		getMainElement().getSubSettings().getElements().add(8, fileSelection);
		getMainElement().getSubSettings().getElements().add(8, new HeaderSetting());

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

			displayAchievement("§aDatei wurde geladen", "§aDatei konnte erfolgreich geladen werden.");
		} catch (IllegalStateException | NullPointerException | JsonSyntaxException e) {
			displayAchievement("§cFehler", "§cDatei konnte nicht geladen werden - Ist das JSON gültig?");
		} catch (Throwable e) {
			displayAchievement("§cFehler", "§cDatei konnte nicht geladen werden - Existiert sie?");
		}
	}

	private static class FileStringSetting extends StringSetting {

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
			FileSelection.chooseFile(f -> {
				if (f == null)
					return;

				try {
					URL url = f.toURI().toURL();
					set(url.toString());
					ModTextField textField = Reflection.get(this, "textField");
					textField.setCursorPositionEnd();
				} catch (MalformedURLException e) {
					BugReporter.reportError(e);
					displayAchievement("§cFehler", "§cDatei konnte nicht geladen werden - WTF?");
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
