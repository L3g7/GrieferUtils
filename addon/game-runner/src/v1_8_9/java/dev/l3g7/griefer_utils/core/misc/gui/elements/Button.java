/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class Button extends GuiButton implements Drawable, Clickable {

	private int renderGroup = 0;
	Runnable callback;

	Button(String label) {
		super(0, 0, 0, 0, 0, label);
	}

	public Button x(double x) {
		xPosition = (int) x;
		return this;
	}

	public Button y(double y) {
		yPosition = (int) y;
		return this;
	}

	public Button pos(double x, double y) {
		return x(x).y(y);
	}

	public Button width(double width) {
		setWidth((int) width);
		return this;
	}

	public Button height(double height) {
		this.height = (int) height;
		return this;
	}

	public Button size(double width, double height) {
		return width(width).height(height);
	}

	public Button renderGroup(int group) {
		renderGroup = group;
		return this;
	}

	public Button callback(Runnable callback) {
		this.callback = callback;
		return this;
	}

	@Override
	public void draw(int mouseX, int mouseY, int renderGroup) {
		if (this.renderGroup == renderGroup)
			drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
	}

	@Override
	public void mousePressed(int mouseX, int mouseY, int mouseButton) {
		if (mousePressed(Minecraft.getMinecraft(), mouseX, mouseY) && callback != null)
			callback.run();
	}

}