/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.gui.elements;

import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.ModTextField;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class TextField extends ModTextField implements Drawable, Clickable {

	private static final double LABEL_HEIGHT = 9 * 1.2;

	private String label;
	private double y;
	private int renderGroup = 0;

	TextField(String label) {
		super(0, mc().fontRendererObj, 0, 0, 0, 20);
		this.label = label;
		setMaxStringLength(Integer.MAX_VALUE);
	}

	public TextField x(double x) {
		xPosition = (int) x;
		return this;
	}

	public TextField y(double y) {
		this.y = y;
		yPosition = (int) (y + LABEL_HEIGHT + 5);
		return this;
	}

	public TextField pos(double x, double y) {
		return x(x).y(y);
	}

	public TextField width(double width) {
		this.width = (int) width;
		return this;
	}

	public TextField height(double height) {
		this.height = (int) height;
		return this;
	}

	public TextField size(double width, double height) {
		return width(width).height(height);
	}

	public double bottom() {
		return y + LABEL_HEIGHT + 5 + height;
	}

	public TextField renderGroup(int renderGroup) {
		this.renderGroup = renderGroup;
		return this;
	}

	public TextField label(String label) {
		this.label = label;
		return this;
	}

	public TextField placeholder(String placeholder) {
		setPlaceHolder("§8" + placeholder);
		return this;
	}

	@Override
	public void draw(int mouseX, int mouseY, int renderGroup) {
		if (this.renderGroup != renderGroup)
			return;

		DrawUtils.drawString(label, xPosition, y, 1.2);
		super.drawTextBox();
	}

	@Override
	public void mousePressed(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

}