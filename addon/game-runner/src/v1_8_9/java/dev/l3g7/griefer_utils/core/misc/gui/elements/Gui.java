/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.gui.elements;

import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.misc.gui.elements.SelectButtonGroup.Selectable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnusedReturnValue")
public class Gui extends GuiScreen {

	protected final List<Clickable> clickables = new ArrayList<>();
	protected final List<Drawable> drawables = new ArrayList<>();
	protected final List<TextField> textFields = new ArrayList<>();

	private <T> T create(T obj) {
		if (obj instanceof Clickable)
			clickables.add((Clickable) obj);
		if (obj instanceof Drawable)
			drawables.add((Drawable) obj);
		if (obj instanceof TextField)
			textFields.add((TextField) obj);
		return obj;
	}

	public Button createButton(String label) {
		return create(new Button(label));
	}

	public Text createCenteredText(String text, double size) {
		return create(new Text(text, size, true));
	}

	public TextField createTextField(String label) {
		return create(new TextField(label));
	}

	public ImageSelection createImageSelection(String label) {
		return create(new ImageSelection(label));
	}

	public <E extends Enum<E> & Selectable> SelectButtonGroup<E> createSelectGroup(E placeholder, String label) {
		return create(new SelectButtonGroup<>(placeholder, label, width));
	}

	public <T extends Enum<T> & Named> DropDown<T> createDropDown(T placeholder, String label) {
		return create(DropDown.fromEnum(placeholder, label, width));
	}

	public <T extends ItemStack> DropDown<T> createDropDown(T placeholder, List<T> values, String label) {
		return create(DropDown.fromItemStack(placeholder, values, label, width));
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		for (Clickable clickable : clickables)
			clickable.mousePressed(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		for (Clickable clickable : clickables)
			clickable.mouseRelease(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
		for (Clickable clickable : clickables)
			clickable.mouseClickMove(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		super.keyTyped(typedChar, keyCode);
		for (TextField textField : textFields)
			textField.textboxKeyTyped(typedChar, keyCode);
	}

	public void draw(int mouseX, int mouseY, int layer) {
		for (Drawable drawable : drawables)
			drawable.draw(mouseX, mouseY, layer);
	}

	public void draw(int mouseX, int mouseY) {
		draw(mouseX, mouseY, 0);
	}

	@Override
	public void setWorldAndResolution(Minecraft mc, int width, int height) {
		clickables.clear();
		drawables.clear();
		super.setWorldAndResolution(mc, width, height);
	}
}