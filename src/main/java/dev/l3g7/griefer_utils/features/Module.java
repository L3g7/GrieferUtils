/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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
import dev.l3g7.griefer_utils.util.misc.Constants;
import dev.l3g7.griefer_utils.util.misc.ServerCheck;
import net.labymod.ingamegui.ModuleCategory;
import net.labymod.ingamegui.ModuleCategoryRegistry;
import net.labymod.ingamegui.moduletypes.SimpleTextModule;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;

/**
 * description missing.
 */
public abstract class Module extends SimpleTextModule {

	public static final ModuleCategory CATEGORY = new ModuleCategory(Constants.ADDON_NAME, true, new ControlElement.IconData("griefer_utils/icons/icon.png"));

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

}