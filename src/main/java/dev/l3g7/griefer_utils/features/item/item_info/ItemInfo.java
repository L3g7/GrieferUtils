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

package dev.l3g7.griefer_utils.features.item.item_info;

import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.item.item_info.info_suppliers.RepairValueViewer;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

@Singleton
public class ItemInfo extends Feature {

	private final List<ItemInfoSupplier> infoSuppliers = FileProvider.getClassesWithSuperClass(ItemInfoSupplier.class).stream()
		.map(ClassMeta::load)
		.map(FileProvider::getSingleton)
		.map(ItemInfoSupplier.class::cast)
		.collect(Collectors.toList());

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Item-Infos")
		.description("Zeigt unterschiedliche Informationen unter einem Item an.")
		.icon("info");

	@Override
	public void init() {
		super.init();
		for (ItemInfoSupplier supplier : infoSuppliers)
			supplier.init(getConfigKey());

		infoSuppliers.sort(Comparator.comparing(f -> f.mainElement.getDisplayName()));
		enabled.subSettings(infoSuppliers.stream().map(s -> s.mainElement).toArray(SettingsElement[]::new));
	}


	@EventListener
	public void onTooltip(ItemTooltipEvent e) {
		for (ItemInfoSupplier infoSupplier : infoSuppliers) {
			if (infoSupplier.isEnabled())
				e.toolTip.addAll(infoSupplier.getToolTip(e.itemStack));
		}
	}

	/**
	 * Fixes direct book interactions.
	 */
	@EventListener
	public void onMouse(MouseEvent event) {
		if (!event.buttonstate || player() == null || mc().currentScreen != null)
			return;

		new RepairValueViewer().init("");
	}

	public static abstract class ItemInfoSupplier {

		protected SettingsElement mainElement;

		protected SettingsElement init(String parentConfigKey) {
			return mainElement = ElementBuilder.initMainElement(this, parentConfigKey).getLeft();
		}

		public abstract List<String> getToolTip(ItemStack itemStack);

		public boolean isEnabled() {
			return ((BooleanSetting) mainElement).get();
		}

	}

}
