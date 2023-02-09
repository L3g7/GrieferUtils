package dev.l3g7.griefer_utils.util.misc;

import dev.l3g7.griefer_utils.util.MinecraftUtil;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
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
