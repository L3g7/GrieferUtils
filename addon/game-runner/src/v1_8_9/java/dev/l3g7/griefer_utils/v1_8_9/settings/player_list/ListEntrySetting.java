/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.settings.player_list;

import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;

public abstract class ListEntrySetting extends ControlElement {

	private final boolean deletable, editable, movable;

	private boolean hoveringDelete = false;
	private boolean hoveringEdit = false;
	private boolean hoveringUp = false;
	private boolean hoveringDown = false;
	private boolean hasIcon;

	public SettingsElement container;

	public ListEntrySetting(boolean deletable, boolean editable, boolean movable, IconData icon) {
		super("§f", icon);
		setSettingEnabled(false);
		this.deletable = deletable;
		this.editable = editable;
		this.movable = movable;
	}

	abstract protected void onChange();

	protected void openSettings() {
		throw new IllegalStateException("unimplemented");
	}

	protected void remove() {
		container.getSubSettings().getElements().remove(this);
		onChange();
		mc.currentScreen.initGui();
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (!mouseOver)
			return;

		if (hoveringEdit) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1));
			openSettings();
			return;
		}

		if (hoveringDelete) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1));
			remove();
			return;
		}

		if (!hoveringUp && !hoveringDown)
			return;

		List<SettingsElement> settings = container.getSubSettings().getElements();
		int index = settings.indexOf(this);
		settings.remove(this);
		settings.add(index + (hoveringDown ? 1 : -1), this);
		onChange();
		mc.currentScreen.initGui();
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		hideSubListButton();
		super.draw(x, y, maxX, maxY, mouseX, mouseY);

		mouseOver = mouseX > x && mouseX < maxX && mouseY > y && mouseY < maxY;
		if (!mouseOver)
			return;

		int xPosition = maxX - 20;
		double yPosition = y + 4.5;

		if (deletable) {
			hoveringDelete = mouseX >= xPosition && mouseY >= yPosition && mouseX <= xPosition + 15.5 && mouseY <= yPosition + 16;

			mc.getTextureManager().bindTexture(new ResourceLocation("labymod/textures/misc/blocked.png"));
			LabyMod.getInstance().getDrawUtils().drawTexture(maxX - (hoveringDelete ? 20 : 19), y + (hoveringDelete ? 3.5 : 4.5), 256, 256, hoveringDelete ? 16 : 14, hoveringDelete ? 16 : 14);
		}

		if (editable) {
			xPosition -= 20;
			hoveringEdit = mouseX >= xPosition && mouseY >= yPosition && mouseX <= xPosition + 15.5 && mouseY <= yPosition + 16;

			mc.getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/pencil.png"));
			LabyMod.getInstance().getDrawUtils().drawTexture(maxX - (hoveringEdit ? 40 : 39), y + (hoveringEdit ? 3.5 : 4.5), 256, 256, hoveringEdit ? 16 : 14, hoveringEdit ? 16 : 14);
		}

		if (!movable)
			return;

		mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/server_selection.png"));

		List<SettingsElement> allSettings = container.getSubSettings().getElements();
		List<SettingsElement> settings = allSettings.stream()
			.filter(s -> getClass().isInstance(s))
			.collect(Collectors.toList());

		int index = settings.indexOf(this);
		hoveringUp = index != 0;

		// Check if the button should exist at all
		xPosition -= 19;
		yPosition = y + 1.5;
		if (hoveringUp) {
			hoveringUp = mouseX >= xPosition && mouseY >= yPosition && mouseX <= xPosition + 44 / 3d && mouseY <= yPosition + 28 / 3d;
			LabyMod.getInstance().getDrawUtils().drawTexture(maxX - 59, y + 1.5, 99, hoveringUp ? 37 : 5, 14, 7, 14 / 0.75d, 7 / 0.75d);
		}

		// Check if the button should exist at all
		hoveringDown = index != settings.size() - 1;
		yPosition += 11;
		if (hoveringDown) {
			hoveringDown = mouseX >= xPosition && mouseY >= yPosition && mouseX <= xPosition + 44 / 3d && mouseY <= yPosition + 28 / 3d;
			LabyMod.getInstance().getDrawUtils().drawTexture(maxX - 59, y + 12.5, 67, hoveringDown ? 52 : 20, 14, 7, 14 / 0.75d, 7 / 0.75d);
		}
	}

}
