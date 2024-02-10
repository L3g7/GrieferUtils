/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.item.item_saver.tool_saver;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.event.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.laby4.events.SettingActivityInitEvent;
import dev.l3g7.griefer_utils.laby4.settings.AbstractSettingImpl;
import dev.l3g7.griefer_utils.laby4.settings.BaseSettingImpl;
import dev.l3g7.griefer_utils.laby4.settings.SettingsImpl;
import dev.l3g7.griefer_utils.settings.AbstractSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.WindowClickEvent;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.activity.settings.SettingWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.accessor.impl.ConfigPropertySettingAccessor;
import net.labymod.api.configuration.settings.type.list.ListSetting;
import net.labymod.api.util.KeyValue;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;
import static net.labymod.api.Textures.SpriteCommon.X;

public class ToolProtectionListSetting extends ListSetting implements BaseSettingImpl<ToolProtectionListSetting, List<ToolProtectionListSetting.ToolProtection>> {

	private final ExtendedStorage<List<ToolProtection>> storage;

	/**
	 * The screen that triggered the current tool add selection.
	 * If null, no selection is active.
	 */
	private GuiScreen previousScreen = null;

	public ToolProtectionListSetting() {
		super(UUID.randomUUID().toString(), null, null, new String[0], null, false, null, (byte) -127,
			new ConfigPropertySettingAccessor(null, null, null, null) {
				@Override
				public <T> T get() {
					return c(new ArrayList<>());
				}

				@Override
				public Type getGenericType() {
					return new ParameterizedType() {
						public Type[] getActualTypeArguments() {return new Type[]{Void.class};} // NOTE: cleanup / generalize

						public Type getRawType() {return null;}

						public Type getOwnerType() {return null;}
					};
				}
			}
		);

		storage = new ExtendedStorage<>(list -> {
			JsonObject obj = new JsonObject();
			for (ToolProtection item : list)
				obj.addProperty(item.name(), ItemUtil.serializeNBT(item.stack()));

			return obj;
		}, elem -> {
			List<ToolProtection> list = new ArrayList<>();
			for (Map.Entry<String, JsonElement> entry : elem.getAsJsonObject().entrySet())
				list.add(new ToolProtection(ItemUtil.fromNBT(entry.getValue().getAsString()), entry.getKey()));

			return list;
		}, new ArrayList<>());

		EventRegisterer.register(this);
	}

	@Override
	public ExtendedStorage<List<ToolProtection>> getStorage() {
		return storage;
	}

	@Override
	public Component displayName() {
		return Component.text(name());
	}

	@Override
	public Component getDescription() {
		String description = storage.description;
		return description == null ? null : Component.text(description);
	}

	@Override
	public Icon getIcon() {
		return getStorage().icon;
	}

	@Override
	public List<KeyValue<Setting>> getElements() {
		List<KeyValue<Setting>> list = new ArrayList<>();

		List<ToolProtection> values = get();

		// Add entries
		for (int i = 0; i < values.size(); i++) {
			ToolProtection protection = values.get(i);

			ToolProtectionEntry entry = new ToolProtectionEntry(i);
			entry.name(protection.name())
				.icon(protection.stack());

			entry.getSubSettings().forEach(s -> ((AbstractSetting<?, ?>) s).callback(this::notifyChange));

			entry.create(this);
			list.add(new KeyValue<>(entry.getId(), entry));
		}

		return list;
	}

	@EventListener
	private void onInit(SettingActivityInitEvent event) {
		if (event.holder() != this)
			return;

		// Update ToolProtectionEntry widgets
		for (Widget w : event.settings().getChildren()) {
			if (w instanceof SettingWidget s && s.setting() instanceof ToolProtectionEntry entry) {
				SettingsImpl.hookChildAdd(s, e -> {
					if (e.childWidget() instanceof FlexibleContentWidget content) {
						content.removeChild("advanced-button");

						content.addContent(ButtonWidget.icon(X, () -> {
							get().remove(entry.index);
							notifyChange();
							event.activity.reload();
						}).addId("delete-button"));
					}
				});
			}
		}

		// Hook add button
		event.get("setting-header", "add-button").setPressable(() -> {
			if (mc().thePlayer == null) {
				labyBridge.notify("§e§lFehler ⚠", "§eHinzufügen von Items ist nur Ingame möglich!");
				return;
			}

			previousScreen = mc().currentScreen;
			display(Constants.ADDON_PREFIX + "Bitte klicke das Item an, das du hinzufügen möchtest.");
			mc().displayGuiScreen(null);
		});

	}

	@EventListener
	private void onAddItem(WindowClickEvent event) {
		if (previousScreen == null || event.itemStack == null)
			return;

		mc().displayGuiScreen(previousScreen);
		previousScreen = null;
		event.cancel();

		if (!event.itemStack.isItemStackDamageable()) {
			labyBridge.notify("§e§lFehler ⚠", "§eDieses Item ist nicht vom Werkzeug-Saver betroffen!");
			return;
		}

		if (isExcluded(event.itemStack)) {
			labyBridge.notify("§e§lFehler ⚠", "§eDieses Item ist bereits ausgenommen!");
			return;
		}

		get().add(new ToolProtection(prepareStack(event.itemStack), event.itemStack.getDisplayName()));
		notifyChange();
	}

	public boolean isExcluded(ItemStack stack) {
		if (stack == null)
			return false;

		String nbt = ItemUtil.serializeNBT(prepareStack(stack));

		for (ToolProtection protection : get()) // NOTE: better NBT comparison
			if (nbt.equals(ItemUtil.serializeNBT(protection.stack())))
				return true;

		return false;
	}

	private ItemStack prepareStack(ItemStack stack) {
		ItemStack is = stack.copy();
		is.stackSize = 1;

		if (is.isItemStackDamageable())
			is.setItemDamage(0);

		if (is.hasTagCompound()) {
			is.getTagCompound().removeTag("display");
			is.getTagCompound().removeTag("RepairCost");
			if (is.getTagCompound().hasNoTags())
				is.setTagCompound(null);
		}

		return is;
	}

	private static class ToolProtectionEntry extends AbstractSettingImpl<ToolProtectionEntry, Object> implements AbstractSetting<ToolProtectionEntry, Object> {

		private final int index;

		public ToolProtectionEntry(int index) {
			super(e -> JsonNull.INSTANCE, e -> NULL, NULL);
			this.index = index;
		}

		@Override
		protected Widget[] createWidgets() {
			return null;
		}

		@Override
		public boolean hasAdvancedButton() {
			return true;
		}

	}

	public record ToolProtection(ItemStack stack, String name) {}

}
