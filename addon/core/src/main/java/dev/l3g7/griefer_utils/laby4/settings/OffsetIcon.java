/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby4.settings;

import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.render.batch.ResourceRenderContext;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.client.resources.CompletableResourceLocation;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.util.bounds.Rectangle;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class OffsetIcon extends Icon {

	private final float offsetX, offsetY;
	private final Icon icon;

	public OffsetIcon(Icon icon, float offsetX, float offsetY) {
		super(null);
		this.icon = icon;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	@Override
	public void render(Stack stack, float x, float y, float width, float height, boolean hover, int color, Rectangle stencil) {
		icon.render(stack, x + offsetX, y + offsetY, width, height, hover, color, stencil);
	}

	@Override
	public void render(ResourceRenderContext context, float x, float y, float width, float height, boolean hover, int color) {
		icon.render(context, x + offsetX, y + offsetY, width, height, hover, color);
	}

}
