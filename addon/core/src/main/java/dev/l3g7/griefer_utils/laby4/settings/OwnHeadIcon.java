/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby4.settings;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.event.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.api.event.events.AccountSwitchEvent;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.render.batch.ResourceRenderContext;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.util.bounds.Rectangle;

import static dev.l3g7.griefer_utils.api.bridges.MinecraftBridge.minecraftBridge;

public class OwnHeadIcon extends Icon {

	Icon icon;

	public OwnHeadIcon() {
		super(null);
		icon = Icon.head(minecraftBridge.uuid());
		EventRegisterer.register(this);
	}

	@EventListener
	private void onAccountSwitch(AccountSwitchEvent event) {
		icon = Icon.head(minecraftBridge.uuid());
	}

	@Override
	public void render(Stack stack, float x, float y, float width, float height, boolean hover, int color, Rectangle stencil) {
		icon.render(stack, x, y, width, height, hover, color, stencil);
	}

	@Override
	public void render(ResourceRenderContext context, float x, float y, float width, float height, boolean hover, int color) {
		icon.render(context, x, y, width, height, hover, color);
	}

}
