/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.misc;

import dev.l3g7.griefer_utils.v1_8_9.util.render.AsyncSkullRenderer;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.util.bounds.Rectangle;

public class SkullIcon extends Icon {

	public static final SkullIcon OWN = new SkullIcon(null);

	private final String name;

	protected SkullIcon(String name) {
		super(null);
		this.name = name;
	}

	public void render(Stack stack, float x, float y, float width, float height, boolean hover, int color, Rectangle stencil) {
		if (this == OWN)
			AsyncSkullRenderer.renderPlayerSkull((int) x, (int) y);
		else
			AsyncSkullRenderer.renderSkull((int) x, (int) y, name);
	}

}
