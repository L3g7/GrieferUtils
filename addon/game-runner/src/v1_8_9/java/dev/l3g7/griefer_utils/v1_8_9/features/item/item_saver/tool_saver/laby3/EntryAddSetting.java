/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.item.item_saver.tool_saver.laby3;

import com.google.gson.JsonNull;
import dev.l3g7.griefer_utils.api.misc.functions.Runnable;
import dev.l3g7.griefer_utils.laby3.settings.Laby3Setting;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.ModColor;

public class EntryAddSetting extends ControlElement implements Laby3Setting<EntryAddSetting, Object> {

	private final ExtendedStorage<Object> storage = new ExtendedStorage<>(e -> JsonNull.INSTANCE, e -> NULL, NULL);
	private Runnable callback;

	public EntryAddSetting(String displayName) {
		super(displayName, new IconData("labymod/textures/settings/category/addons.png"));
	}

	@Override
	public EntryAddSetting callback(Runnable callback) {
		this.callback = callback;
		return this;
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		mouseOver = mouseX > x && mouseX < maxX && mouseY > y && mouseY < maxY;

		LabyMod.getInstance().getDrawUtils().drawRectangle(x, y, maxX, maxY, ModColor.toRGB(80, 80, 80, 60));
		int iconWidth = iconData != null ? 25 : 2;
		mc.getTextureManager().bindTexture(iconData.getTextureIcon());

		if (mouseOver) {
			LabyMod.getInstance().getDrawUtils().drawTexture(x + 2, y + 2, 256.0, 256.0, 18, 18);
			LabyMod.getInstance().getDrawUtils().drawString(displayName, x + iconWidth + 1, (double) y + 7 - 0);
		} else {
			LabyMod.getInstance().getDrawUtils().drawTexture(x + 3, y + 3, 256.0, 256.0, 16.0, 16.0);
			LabyMod.getInstance().getDrawUtils().drawString(displayName, x + iconWidth, (double) y + 7 - 0);
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseOver)
			callback.run();
	}

	@Override
	public ExtendedStorage<Object> getStorage() {
		return storage;
	}

}