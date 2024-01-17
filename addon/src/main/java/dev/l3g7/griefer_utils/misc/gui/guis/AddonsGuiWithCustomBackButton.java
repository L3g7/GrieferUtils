/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.misc.gui.guis;

import dev.l3g7.griefer_utils.core.misc.functions.Supplier;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
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

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

public class AddonsGuiWithCustomBackButton extends LabyModAddonsGui {

	private final HashSet<Supplier<Boolean>> closeChecks = new HashSet<>();
	private final GuiScreen previousScreen = mc().currentScreen;

	public AddonsGuiWithCustomBackButton(Runnable onBack, SettingsElement element) {
		this(element);
		addCheck(() -> { onBack.run(); return true; });
	}

	public AddonsGuiWithCustomBackButton(SettingsElement element) {
		List<SettingsElement> path = new ArrayList<>(MinecraftUtil.path());
		if (element != null)
			path.add(element);

		Reflection.set(this, path, "path");

		List<SettingsElement> previousPath = new ArrayList<>(path);
		previousPath.remove(previousPath.size() - 1);
		Reflection.set(previousScreen, previousPath, "path");


		AddonElement openAddon = Reflection.get(mc().currentScreen, "openedAddonSettings");
		Reflection.set(this, openAddon, "openedAddonSettings");
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
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button == Reflection.get(this, "buttonBack")) {
			if (canClose())
				mc().displayGuiScreen(previousScreen);

			return;
		}

		super.actionPerformed(button);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == 1 && !canClose())
			return;

		super.keyTyped(typedChar, keyCode);
	}

	static {
		Tabs.getTabUpdateListener().add(m -> {

			Class<? extends GuiScreen>[] addonTabs = m.get("tab_addons");
			Class<? extends GuiScreen>[] newAddonTabs = new Class[addonTabs.length + 1];

			System.arraycopy(addonTabs, 0, newAddonTabs, 0, addonTabs.length);
			newAddonTabs[newAddonTabs.length - 1] = AddonsGuiWithCustomBackButton.class;
			m.put("tab_addons", newAddonTabs);
		});
	}

}
