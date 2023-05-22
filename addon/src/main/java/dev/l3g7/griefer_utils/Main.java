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

package dev.l3g7.griefer_utils;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.event.AnnotationEventHandler;
import dev.l3g7.griefer_utils.event.EventHandler;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.uncategorized.settings.auto_update.AutoUpdate;
import dev.l3g7.griefer_utils.misc.MissingForgeErrorGui;
import dev.l3g7.griefer_utils.settings.MainPage;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.addon.AddonLoader;
import net.labymod.addon.online.AddonInfoManager;
import net.labymod.addon.online.info.AddonInfo;
import net.labymod.api.LabyModAddon;
import net.labymod.core.asm.LabyModCoreMod;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.crash.CrashReport;
import net.minecraftforge.client.event.GuiOpenEvent;

import java.util.List;
import java.util.UUID;

/**
 * The main class.
 */
public class Main extends LabyModAddon {

	private static Main instance;

	public static Main getInstance() {
		return instance;
	}

	public Main() {
		instance = this;
	}

	@Override
	public void onEnable() {
		System.out.println("GrieferUtils enabling");
		long begin = System.currentTimeMillis();
		if (!LabyModCoreMod.isForge()) {
			MissingForgeErrorGui.open();
			return;
		}

		FileProvider.getClassesWithSuperClass(Feature.class).forEach(meta -> {
			if (meta.isAbstract())
				return;

			Feature instance = FileProvider.getSingleton(meta.load());
			try {
				instance.init();
			} catch (RuntimeException t) {
				MinecraftUtil.mc().displayCrashReport(new CrashReport("GrieferUtils konnte nicht geladen werden!", t));
			}
		});

		try {
			EventHandler.init();
			AnnotationEventHandler.triggerEvent(OnEnable.class);
		} catch (Throwable t) {
			MinecraftUtil.mc().displayCrashReport(new CrashReport("GrieferUtils konnte nicht geladen werden!", t));
		}
		System.out.println("GrieferUtils enabled! (took " + (System.currentTimeMillis() - begin) + " ms)");
	}

	/**
	 * Ensures GrieferUtils is shown in the {@link LabyModAddonsGui}.<br>
	 * Fixes an incompatibility with HDSkins (wtf)
	 */
	@EventListener
	private static void onGuiOpen(GuiOpenEvent event) {
		if (!(event.gui instanceof LabyModAddonsGui))
			return;

		UUID uuid = Main.getInstance().about.uuid;
		for (AddonInfo addonInfo : AddonInfoManager.getInstance().getAddonInfoList())
			if (addonInfo.getUuid().equals(uuid))
				return;

		for (AddonInfo offlineAddon : AddonLoader.getOfflineAddons()) {
			if (offlineAddon.getUuid().equals(uuid)) {
				AddonInfoManager.getInstance().getAddonInfoList().add(offlineAddon);
				return;
			}
		}

		throw new RuntimeException("GrieferUtils couldn't be loaded");
	}

	@Override
	public void loadConfig() {
		AutoUpdate.checkForUpdate(about.uuid);
	}

	@Override
	protected void fillSettings(List<SettingsElement> list) {
		if (!LabyModCoreMod.isForge())
			return;

		list.addAll(MainPage.settings);
	}

}
