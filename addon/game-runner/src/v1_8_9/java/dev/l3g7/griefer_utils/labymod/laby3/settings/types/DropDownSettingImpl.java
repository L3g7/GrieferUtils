/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.settings.types;

import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.misc.functions.Function;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.labymod.laby3.settings.Laby3Setting;
import net.labymod.gui.elements.DropDownMenu;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.DropDownElement;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.ModColor;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

public class DropDownSettingImpl<E extends Enum<E> & Named> extends DropDownElement<E> implements Laby3Setting<DropDownSetting<E>, E>, DropDownSetting<E> {

	private final ExtendedStorage<E> storage;
	private final FixedDropDownMenu<E> menu = new FixedDropDownMenu<>();

	public DropDownSettingImpl(Class<E> enumClass) {
		super("§cNo name set", null);
		setChangeListener(this::set);

		// Initialize name getter
		Function<Object, String> getName;
		if (Arrays.stream(enumClass.getInterfaces()).anyMatch(c -> c == Named.class)) {
			getName = e -> ((Named) e).getName();
		} else {
			// Fallback to old Named
			Optional<Class<?>> oldNamed = Arrays.stream(enumClass.getInterfaces())
				.filter(c -> c.getName().equals("dev.l3g7.griefer_utils.misc.Named"))
				.findFirst();

			//noinspection SimplifyOptionalCallChains
			if (!oldNamed.isPresent())
				throw new ClassCastException("Cannot convert " + enumClass + " to Named!");

			try {
				MethodHandle MH_getName = lookup().findVirtual(oldNamed.get(), "getName", methodType(String.class));
				getName = e -> (String) MH_getName.invoke(e);
			} catch (NoSuchMethodException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		// Initialize storage
		storage = new ExtendedStorage<>(e -> new JsonPrimitive(e.name()), s -> Enum.valueOf(enumClass, s.getAsString()), enumClass.getEnumConstants()[0]);

		// Initialize menu
		menu.fill(enumClass.getEnumConstants());
		Reflection.set(this, "dropDownMenu", menu);

		// Use Named interface as provider for display names
		DrawUtils drawUtils = LabyMod.getInstance().getDrawUtils();
		menu.setEntryDrawer((o, x, y, trimmedEntry) -> drawUtils.drawString(getName.apply(o), x, y));
		int width = ((List<?>) Reflection.get(menu, "list"))
			.stream().mapToInt(e -> drawUtils.getStringWidth((getName.apply(e))))
			.max()
			.orElse(0)
			+ 9;

		menu.doSetWidth(width + 15); // 15px for arrow
	}

	@Override
	public ExtendedStorage<E> getStorage() {
		return storage;
	}

	@Override
	public DropDownSetting<E> name(String name) {
		if (getIconData() == null)
			menu.setTitle(name);
		return Laby3Setting.super.name(name);
	}

	@Override
	public DropDownSetting<E> icon(Object icon) {
		menu.setTitle("");
		return Laby3Setting.super.icon(icon);
	}

	@Override
	public DropDownSetting<E> set(E value) {
		menu.setSelected(value);
		return Laby3Setting.super.set(value);
	}

	/**
	 * Disables sub settings for DropDownSetting, as this is not implemented in rendering.
	 */
	@Override
	public DropDownSetting<E> subSettings(List<BaseSetting<?>> settings) {
		throw new UnsupportedOperationException("unimplemented");
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {

		// Adapt dropdown width to name
		int originalWidth = menu.getWidth();
		String name = getDisplayName();
		if (name.contains(" "))
			name = Arrays.stream(name.split(" ")).max(Comparator.comparingInt(String::length)).orElse("§c");

		menu.doSetWidth(Math.min(menu.getWidth(), maxX - x - 35 - mc.fontRendererObj.getStringWidth(name))); // dropdown width - 35px for icon and padding - name

		DrawUtils drawUtils = LabyMod.getInstance().getDrawUtils();

		// Reset selection, so selected value isn't rendered
		E selected = menu.getSelected();
		menu.setSelected(null);
		int width = menu.getWidth();

		menu.doSetX(maxX - width - 5);
		super.draw(x, y, maxX, maxY, mouseX, mouseY);

		menu.setSelected(selected);

		int height = maxY - y - 6;

		// Draw selected entry with fixed width
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(0, 0, (int) ((maxX - 21) / new ScaledResolution(mc).getScaledWidth_double() * mc.displayWidth), mc.displayHeight);

		String trimmedEntry = drawUtils.trimStringToWidth(ModColor.cl("f") + selected.getName(), width - 10);
		drawUtils.drawString(trimmedEntry, menu.getX() + 5, (y + 3) + height / 2f - 4);

		GL11.glDisable(GL11.GL_SCISSOR_TEST);

		// Draw gradient
		drawUtils.drawGradientShadowRight(maxX - 21, y + 3, maxY - 3);
		drawUtils.drawGradientShadowRight(maxX - 21, y + 3, maxY - 3);

		// Hide overlapping pixels
		Gui.drawRect(maxX - 21, maxY - 13, maxX - 20, maxY - 3, 0xFF000000);

		// Draw dropdown with fixed width
		if (!menu.isOpen())
			return;

		menu.doSetWidth(originalWidth);
		menu.drawMenuDirect(menu.getX(), menu.getY(), mouseX, mouseY);
	}

	/**
	 * A dropdown menu with overwritten setWidth and setX methods to prevent LabyMod from
	 * changing the bounds when rendering.
	 */
	private static class FixedDropDownMenu<E> extends DropDownMenu<E> {

		public FixedDropDownMenu() {
			super("", 0, 0, 0, 0);
		}

		@Override
		public void setWidth(int width) {}

		@Override
		public void setX(int x) {}

		public void doSetWidth(int width) {
			super.setWidth(width);
		}

		public void doSetX(int x) {
			super.setX(x);
		}

	}

}
