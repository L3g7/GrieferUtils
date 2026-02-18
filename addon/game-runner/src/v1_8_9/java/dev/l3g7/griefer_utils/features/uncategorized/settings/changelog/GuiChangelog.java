/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.settings.changelog;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.misc.server.requests.StaticApiRequest.StaticApiData.ChangelogEntry;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.misc.gui.guis.SubtleGuiButton;
import dev.l3g7.griefer_utils.core.misc.gui.guis.TextList;
import dev.l3g7.griefer_utils.features.uncategorized.settings.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.Map.Entry;

import static dev.l3g7.griefer_utils.core.api.event_bus.Priority.LOWEST;

public class GuiChangelog extends GuiScreen {

	private static GuiScreen previousScreen;

	private final boolean showDisableButton;
	private final ChangelogEntry changelog;
	private final String version;

	private TextList textList;

	// Make sure the gui closes to the correct screen
	@EventListener(priority = LOWEST)
	private static void onGuiOpen(GuiOpenEvent<?> event) {
		if (event.isCanceled() || event.gui instanceof GuiChangelog)
			return;

		previousScreen = event.gui;
	}

	public GuiChangelog(boolean showDisableButton, ChangelogEntry changelog, String version) {
		this.showDisableButton = showDisableButton;
		this.changelog = changelog;
		this.version = version;
	}

	@Override
	public void initGui() {
		super.initGui();

		textList = new TextList(mc, width, height, 64, height - 42, fontRendererObj);
		textList.addEntry("");

		boolean isStart = true;
		for (Entry<String, String[]> block : changelog.changelog.entrySet()) {
			if (!isStart)
				textList.addEntry("");

			textList.addEntry(block.getKey() + ":");
			for (String change : block.getValue())
				textList.addEntry("    - " + change);

			isStart = false;
		}
		textList.addEntry("");

		buttonList.clear();
		buttonList.add(new GuiButton(0, width / 2 + 4 + 75, height - 28, 150, 20, "Schließen"));
		if (showDisableButton)
			buttonList.add(new SubtleGuiButton(1, width / 2d - 186, height - 22, "Nicht nochmal anzeigen"));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (textList == null) {
			setWorldAndResolution(mc, 1, 1);
			this.initGui();
			return;
		}

		drawBackground(0);
		textList.drawScreen(mouseX, mouseY, partialTicks);

		String text = "§nGrieferUtils - Changelog - v" + version;

		// Title
		GlStateManager.scale(1.5, 1.5, 1.5);
		drawCenteredString(fontRendererObj, text, width / 3, 15, 0xffffff);
		GlStateManager.scale(1 / 1.5, 1 / 1.5, 1 / 1.5);

		// Icon
		int textWidth = fontRendererObj.getStringWidth(text);
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("griefer_utils", "icons/high_res/icon.png"));
		DrawUtils.drawTexture(width / 2d - textWidth * 0.75 - 29, 18, 256, 256, 20, 20);

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {}

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();
		textList.handleMouseInput();
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		textList.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 1)
			Settings.showUpdateScreen.set(false);

		// Close GUI on disableButton and on closeButton
		mc.displayGuiScreen(previousScreen);
	}

}
