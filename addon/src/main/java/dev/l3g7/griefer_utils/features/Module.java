/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

package dev.l3g7.griefer_utils.features;

import dev.l3g7.griefer_utils.event.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.util.misc.Constants;
import dev.l3g7.griefer_utils.util.misc.ServerCheck;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.ingamegui.ModuleCategory;
import net.labymod.ingamegui.ModuleCategoryRegistry;
import net.labymod.ingamegui.enums.EnumDisplayType;
import net.labymod.ingamegui.enums.EnumModuleFormatting;
import net.labymod.ingamegui.moduletypes.SimpleModule;
import net.labymod.ingamegui.moduletypes.SimpleTextModule;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;

import java.util.List;

public abstract class Module extends SimpleTextModule {

	public static final ModuleCategory CATEGORY = new ModuleCategory(Constants.ADDON_NAME, true, new ControlElement.IconData("griefer_utils/icons/icon.png")) {
		@Override
		public void createCategoryElement() {
			super.createCategoryElement();

			// Inject headers
			SimpleModule stylingModule = new SimpleModule(){
				public String getDisplayName() { return ""; }
				public String getDisplayValue() { return ""; }
				public String getDefaultValue() { return ""; }
				public String getSettingName() { return ""; }
				public String getDescription() { return null; }
				public void loadSettings() {}
				public int getSortingId() { return 0; }
				public ControlElement.IconData getIconData() { return null; }
				public boolean isEnabled(EnumDisplayType displayType) { return true; }
			};

			List<SettingsElement> elems = CATEGORY.getCategoryElement().getSubSettings().getElements();

			int offset = 0;
			elems.add(offset++, new HeaderSetting().entryHeight(8));
			elems.add(offset++, new HeaderSetting("§r§l" + Constants.ADDON_NAME).scale(1.3));
			elems.add(offset++, new HeaderSetting("Geld-Statistiken"));
			elems.add(3 + offset++, new HeaderSetting("Orb-Statistiken"));
			elems.add(5 + offset++, new HeaderSetting("Countdowns"));
			elems.add(8 + offset  , new HeaderSetting("Misc"));

			for (SettingsElement elem : elems)
				if (((ControlElement) elem).getModule() == null)
					Reflection.set(elem, stylingModule, "module");
		}
	};

	@OnEnable
	public static void register() {
		ModuleCategoryRegistry.loadCategory(CATEGORY);

		FileProvider.getClassesWithSuperClass(Module.class).stream()
			.map(meta -> (Module) FileProvider.getSingleton(meta.load()))
			.sorted((a, b) -> (a.getClass().getPackage().getName() + a.getControlName()).compareToIgnoreCase((b.getClass().getPackage().getName() + b.getControlName()))) // Include package in sorting so the modules are grouped
			.forEach(LabyMod.getInstance().getLabyModAPI()::registerModule);
	}

	private final String name;
	private final String description;
	private final String configKey;
	private final ControlElement.IconData iconData;

	public Module(String name, String description, String configKey, ControlElement.IconData iconData) {
		this.name = name;
		this.description = description;
		this.configKey = configKey;
		this.iconData = iconData;
	}

	public String getControlName() { return name; }

	public String[] getKeys() { return new String[]{name}; }
	public String[] getDefaultKeys() { return new String[]{name}; }

	public ControlElement.IconData getIconData() { return iconData; }
	public String getSettingName() { return configKey; }
	public String getDescription() { return description; }
	public boolean isShown() { return !LabyMod.getInstance().isInGame() || ServerCheck.isOnGrieferGames(); }
	public boolean isActive() { return getBooleanElement().getCurrentValue(); }

	public void loadSettings() {}
	public int getSortingId() { return 0; }
	public ModuleCategory getCategory() { return CATEGORY; }
	public EnumModuleFormatting getDisplayFormatting() { return super.getDisplayFormatting(); }

	@Override
	public void fillSubSettings(List<SettingsElement> list) {
		list.add(new HeaderSetting().entryHeight(8));
		list.add(new HeaderSetting("§r§l" + Constants.ADDON_NAME).scale(1.3));
		list.add(new HeaderSetting(getControlName()));
		super.fillSubSettings(list);
		list.add(new HeaderSetting());
	}

}