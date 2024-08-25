/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.temp;

import dev.l3g7.griefer_utils.core.api.misc.functions.Supplier;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import net.labymod.gui.elements.Tabs;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.AddonElement;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class TempAddonsGuiWithCustomBackButton extends LabyModAddonsGui {

	private final HashSet<Supplier<Boolean>> closeChecks = new HashSet<>();
	private final GuiScreen previousScreen = mc().currentScreen;
	private final int startPathSize;

	public TempAddonsGuiWithCustomBackButton(Runnable onBack, SettingsElement element) {
		this(element);
		addCheck(() -> { onBack.run(); return true; });
	}

	public static ArrayList<SettingsElement> path() { return Reflection.get(mc().currentScreen, "path"); }

	public TempAddonsGuiWithCustomBackButton(SettingsElement element) {
		List<SettingsElement> path = new ArrayList<>(path());
		if (element != null)
			path.add(element);

		startPathSize = path.size();

		Reflection.set(this, "path", path);

		List<SettingsElement> previousPath = new ArrayList<>(path);
		previousPath.remove(previousPath.size() - 1);
		Reflection.set(previousScreen, "path", previousPath);


		AddonElement openAddon = Reflection.get(mc().currentScreen, "openedAddonSettings");
		Reflection.set(this, "openedAddonSettings", openAddon);
	}

	public void addCheck(Supplier<Boolean> canClose) {
		closeChecks.add(canClose);
	}

	private boolean canClose() {
		for (Supplier<Boolean> closeCheck : closeChecks)
			if (!closeCheck.get())
				return false;

		return true;
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button == Reflection.get(this, "buttonBack") && startPathSize == path().size()) {
			if (canClose())
				mc().displayGuiScreen(previousScreen);

			return;
		}

		try {
			super.actionPerformed(button);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		if (keyCode == 1 && !canClose())
			return;

		try {
			super.keyTyped(typedChar, keyCode);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		try {
			super.mouseClicked(mouseX, mouseY, mouseButton);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void handleMouseInput() {
		try {
			super.handleMouseInput();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	static {
		Tabs.getTabUpdateListener().add(m -> {

			Class<? extends GuiScreen>[] addonTabs = m.get("tab_addons");
			Class<? extends GuiScreen>[] newAddonTabs = new Class[addonTabs.length + 1];

			System.arraycopy(addonTabs, 0, newAddonTabs, 0, addonTabs.length);
			newAddonTabs[newAddonTabs.length - 1] = TempAddonsGuiWithCustomBackButton.class;
			m.put("tab_addons", newAddonTabs);
		});
	}

}
