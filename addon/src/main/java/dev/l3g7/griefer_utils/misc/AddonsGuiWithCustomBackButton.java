/*
 * This file is part of GrieferUtils https://github.com/L3g7/GrieferUtils.
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 the "License";
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

package dev.l3g7.griefer_utils.misc;

import dev.l3g7.griefer_utils.util.MinecraftUtil;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import net.labymod.gui.elements.Tabs;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.AddonElement;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.ArrayList;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

public class AddonsGuiWithCustomBackButton extends LabyModAddonsGui {

	private final Runnable onBack;

	public AddonsGuiWithCustomBackButton(Runnable onBack, SettingsElement element) {
		this.onBack = onBack;
		ArrayList<SettingsElement> path = MinecraftUtil.path();
		if (element != null)
			path.add(element);

		Reflection.set(this, path, "path");

		AddonElement openAddon = Reflection.get(mc().currentScreen, "openedAddonSettings");
		Reflection.set(this, openAddon, "openedAddonSettings");
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button == Reflection.get(this, "buttonBack"))
			onBack.run();

		super.actionPerformed(button);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == 1)
			onBack.run();

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
