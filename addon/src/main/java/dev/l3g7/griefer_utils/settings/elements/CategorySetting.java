/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.settings.elements;

import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.util.render.RenderUtil;
import net.labymod.settings.elements.ControlElement;

public class CategorySetting extends ControlElement implements ElementBuilder<CategorySetting> {

    public CategorySetting() {
        super("§cNo name set", null);
        setSettingEnabled(true);
        setHoverable(true);
    }

	private final IconStorage iconStorage = new IconStorage();

    @Override
    public int getObjectWidth() {
        return 0;
    }

	@Override
	public IconStorage getIconStorage() {
		return iconStorage;
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		if (RenderUtil.shouldBeCulled(y, maxY))
			return;

		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		drawIcon(x, y);
	}

}