/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.settings.changelog;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.auto_update.ReleaseInfo;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.misc.gui.guis.TextList;
import dev.l3g7.griefer_utils.features.uncategorized.settings.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import static dev.l3g7.griefer_utils.core.api.event_bus.Priority.LOWEST;

public class GuiStable extends GuiScreen {

	private static GuiScreen previousScreen;
	private TextList textList;

	// Make sure the gui closes to the correct screen
	@EventListener(priority = LOWEST)
	private static void onGuiOpen(GuiOpenEvent<?> event) {
		if (event.isCanceled() || event.gui instanceof GuiStable || event.gui instanceof GuiChangelog)
			return;

		previousScreen = event.gui;
	}

	@Override
	public void initGui() {
		super.initGui();

		textList = new TextList(mc, width, height, 64, height - 42, fontRendererObj);

		textList.addEntry("");
		textList.addEntry("Frohes neues Jahr!");
		textList.addEntry("");
		textList.addEntry("Nach 2 Jahren gibt es nun endlich eine neue Stable-Version.");
		textList.addEntry("Die Stable-Version wird ab nun so sein wie die Beta, und die Beta wird für experimentelle Änderungen verwendet.");
		textList.addEntry("");
		textList.addEntry("Möchtest du trotzdem bei der Beta bleiben?");
		textList.addEntry("");

		buttonList.clear();
		buttonList.add(new GuiButton(0, width / 2 + 79, height - 28, 150, 20, "Zu Stable wechseln"));
		buttonList.add(new GuiButton(1, width / 2 - 79 - 150, height - 28, 150, 20, "Bei Beta bleiben"));
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

		String text = "§nGrieferUtils - Stable - v2.3";

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
		if (button.id == 0)
			Settings.releaseChannel.set(ReleaseInfo.ReleaseChannel.STABLE);

		// Close GUI on disableButton and on closeButton
		mc.displayGuiScreen(previousScreen);
	}

}
