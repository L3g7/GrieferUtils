/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.item.item_info;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.Feature.FeatureCategory;
import dev.l3g7.griefer_utils.settings.BaseSetting;
import dev.l3g7.griefer_utils.settings.SettingLoader;
import dev.l3g7.griefer_utils.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.settings.types.KeySetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.ItemTooltipEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.modules.BlockInfo;
import dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.griefer_info.gui.GuiBigChest;
import dev.l3g7.griefer_utils.v1_8_9.misc.TriggerModeSetting;
import dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil;
import net.minecraft.item.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.settings.types.HeaderSetting.create;
import static dev.l3g7.griefer_utils.v1_8_9.misc.TriggerModeSetting.TriggerMode.HOLD;

@Singleton
@FeatureCategory
public class ItemInfo extends Feature {

	private final List<ItemInfoSupplier> infoSuppliers = FileProvider.getClassesWithSuperClass(ItemInfoSupplier.class).stream()
		.map(ClassMeta::load)
		.map(FileProvider::getSingleton)
		.map(ItemInfoSupplier.class::cast)
		.collect(Collectors.toList());

	private final DropDownSetting<TriggerModeSetting.TriggerMode> triggerMode = TriggerModeSetting.create()
		.description("Halten: Zeigt die Item-Infos an, während die Taste gedrückt wird.\nUmschalten: Schaltet das Anzeigen der Item-Infos um, wenn die Taste gedrückt wird.")
		.defaultValue(HOLD)
		.callback(() -> {
			if (getMainElement() != null)
				((SwitchSetting) getMainElement()).set(false);
		});

	private final KeySetting key = KeySetting.create()
		.name("Taste")
		.icon("key")
		.description("Die Taste, mit der die Item-Infos angezeigt werden.")
		.triggersInContainers()
		.pressCallback(p -> {
			if (p || triggerMode.get() == HOLD) {
				SwitchSetting enabled = ((SwitchSetting) getMainElement());
				enabled.set(!enabled.get());
			}
		});

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Item-Infos")
		.description("Zeigt unterschiedliche Informationen unter einem Item an.")
		.icon("info")
		.subSettings(key, triggerMode, create());

	@Override
	public void init() {
		super.init();
		for (ItemInfoSupplier supplier : infoSuppliers)
			supplier.init(getConfigKey());

		infoSuppliers.sort(Comparator.comparing(f -> f.mainElement.name()));
		enabled.subSettings(infoSuppliers.stream().map(s -> s.mainElement).collect(Collectors.toList()));
	}


	@EventListener
	public void onTooltip(ItemTooltipEvent e) {
		if (BlockInfo.gettingTooltip)
			return;

		if (MinecraftUtil.mc().currentScreen instanceof GuiBigChest)
			return;

		for (ItemInfoSupplier infoSupplier : infoSuppliers) {
			if (infoSupplier.isEnabled())
				e.toolTip.addAll(infoSupplier.getToolTip(e.itemStack));
		}
	}

	public static abstract class ItemInfoSupplier {

		public BaseSetting<?> mainElement;

		protected BaseSetting<?> init(String parentKey) {
			return mainElement = SettingLoader.initMainElement(this, parentKey).mainElement;
		}

		public abstract List<String> getToolTip(ItemStack itemStack);

		public boolean isEnabled() {
			return ((SwitchSetting) mainElement).get();
		}

	}

}
