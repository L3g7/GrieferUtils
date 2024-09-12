/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.other;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.labymod.laby4.settings.types.SwitchSettingImpl;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.hud.hudwidget.HudWidget;
import net.labymod.api.client.gui.hud.hudwidget.HudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.item.ItemHudWidget;
import net.labymod.api.client.gui.hud.position.HudSize;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.render.font.RenderableComponent;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.core.client.gui.hud.hudwidget.ItemCounterHudWidget;
import net.labymod.core.client.gui.hud.hudwidget.ItemCounterHudWidget.CountingItem;
import net.labymod.core.client.gui.hud.hudwidget.item.equipment.MainHandHudWidget;
import net.labymod.ingamegui.modules.item.HeldItemModule;
import net.labymod.ingamegui.moduletypes.ItemModule;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.DECIMAL_FORMAT_98;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

/**
 * This isn't a real module, it injects into LabyMod's {@link ItemModule} / {@link CountingItem}.
 */
public class ItemCounter {

	// --------- LabyMod 3 ---------

	public static SwitchSetting enabled = SwitchSetting.create()
		.name("Item-Zähler")
		.description("Zeigt anstatt der Haltbarkeit an, wie viele Items von dem Typ in dem derzeitigen Inventar vorhanden sind.")
		.icon("spyglass")
		.config("modules.held_item_counter.enabled");

	@ExclusiveTo(LABY_3)
	@SuppressWarnings("ConstantValue")
	@Mixin(value = ItemModule.class, remap = false)
	private abstract static class MixinItemModule {

		@Inject(method = "fillSubSettings", at = @At("RETURN"))
		private void injectFillSubSettings(List<SettingsElement> settings, CallbackInfo ci) {
			if (!((Object) this instanceof HeldItemModule))
				return;

			settings.add((SettingsElement) HeaderSetting.create().entryHeight(8));
			settings.add((SettingsElement) HeaderSetting.create("§r§l" + Constants.ADDON_NAME).scale(1.3));
			settings.add((SettingsElement) HeaderSetting.create().entryHeight(8));
			settings.add((SettingsElement) enabled);

			// Copy settings of the original ItemCounter
			List<SettingsElement> subSettings = ((SettingsElement) enabled).getSubSettings().getElements();
			if (subSettings.isEmpty()) {
				subSettings.add((SettingsElement) HeaderSetting.create().entryHeight(8));
				subSettings.add((SettingsElement) HeaderSetting.create("§r§l" + Constants.ADDON_NAME).scale(1.3));
				subSettings.add((SettingsElement) HeaderSetting.create("Item-Zähler"));
				subSettings.add((SettingsElement) HeaderSetting.create().entryHeight(8));
				List<SettingsElement> originalSettings = ((SettingsElement) FileProvider.getSingleton(dev.l3g7.griefer_utils.features.item.item_info.info_suppliers.ItemCounter.class).mainElement).getSubSettings().getElements();
				subSettings.addAll(originalSettings.subList(originalSettings.size() - 3, originalSettings.size()));
			}
		}

		@Redirect(method = "draw(DDDLnet/labymod/ingamegui/enums/EnumItemSlot;)V", at = @At(value = "INVOKE", target = "Lnet/labymod/utils/DrawUtils;drawItem(Lnet/minecraft/item/ItemStack;DDLjava/lang/String;)V"))
		private void drawItem(net.labymod.utils.DrawUtils instance, ItemStack stack, double xPosition, double yPosition, String value) {
			if (!((Object) this instanceof HeldItemModule) || !enabled.get() || stack.isItemStackDamageable()) {
				instance.drawItem(stack, xPosition, yPosition, null);
				return;
			}

			List<ItemStack> itemStacks = Arrays.asList(MinecraftUtil.player().inventory.mainInventory);
			long totalAmount = dev.l3g7.griefer_utils.features.item.item_info.info_suppliers.ItemCounter.getAmount(itemStacks, stack);
			instance.drawItem(stack, xPosition, yPosition, Constants.DECIMAL_FORMAT_98.format(totalAmount));
		}

	}

	// --------- LabyMod 4 ---------

	private static final Map<Object, SwitchSetting> settings = new HashMap<>();

	public static boolean isEnabled(Object object) {
		SwitchSetting setting = settings.get(object);
		return setting != null && setting.get();
	}

	public static void addSetting(Object widget, String configKey) {
		SwitchSettingImpl switchSetting = (SwitchSettingImpl) SwitchSetting.create()
			.name("Item-Zähler")
			.description("Zeigt anstatt der Stack-Größe an, wie viele Items von dem Typ in dem derzeitigen Inventar vorhanden sind.")
			.icon("spyglass")
			.config("modules." + configKey + ".enabled");

		// Copy item counter's settings
		switchSetting.addSetting(dev.l3g7.griefer_utils.features.item.item_info.info_suppliers.ItemCounter.ignoreDamage);
		switchSetting.addSetting(dev.l3g7.griefer_utils.features.item.item_info.info_suppliers.ItemCounter.ignoreEnchants);
		switchSetting.addSetting(dev.l3g7.griefer_utils.features.item.item_info.info_suppliers.ItemCounter.ignoreLore);

		switchSetting.create(switchSetting);

		HudWidget<?> hudWidget = (HudWidget<?>) widget;
		hudWidget.getSettings().add(switchSetting);

		settings.put(widget, switchSetting);
	}

	@ExclusiveTo(LABY_4)
	@Mixin(value = HudWidget.class, remap = false)
	private static class MixinHudWidget {

		@SuppressWarnings("ConstantValue")
		@Inject(method = "load", at = @At("TAIL"))
		private void injectLoad(HudWidgetConfig config, CallbackInfo ci) {
			if (c(this) instanceof ItemCounterHudWidget)
				addSetting(this, "item_counter");
			if (c(this) instanceof MainHandHudWidget)
				addSetting(this, "held_item_counter");
		}

	}

	@ExclusiveTo(LABY_4)
	@Mixin(value = CountingItem.class, remap = false)
	private static abstract class MixinCountingItem {

		@Shadow
		@Final
		private ConfigProperty<net.labymod.api.client.world.item.ItemStack> item;

		@Shadow
		private transient int itemCount;

		@Shadow
		private transient RenderableComponent renderableCount;

		@SuppressWarnings("DataFlowIssue")
		@Inject(method = "onItemCountChange", at = @At("HEAD"), cancellable = true)
		private void injectOnItemCountChange(int count, CallbackInfo ci) {
			if (count == 0 || player() == null || !isEnabled(this))
				return;

			ItemStack stack = (ItemStack) (Object) item.get();
			if (stack.isItemStackDamageable())
				return;

			List<ItemStack> itemStacks = Arrays.asList(player().inventory.mainInventory);
			long totalAmount = dev.l3g7.griefer_utils.features.item.item_info.info_suppliers.ItemCounter.getAmount(itemStacks, stack);
			if (totalAmount == count)
				return;

			itemCount = (int) totalAmount;
			if (itemCount != totalAmount)
				itemCount = -1; // amount exceeds int limit
			renderableCount = RenderableComponent.of(Component.text(DECIMAL_FORMAT_98.format(totalAmount)));
			ci.cancel();
		}

	}

	@ExclusiveTo(LABY_4)
	@Mixin(value = ItemHudWidget.class, remap = false)
	private static abstract class MixinItemHudWidget {

		@Shadow
		private net.labymod.api.client.world.item.ItemStack itemStack;

		@SuppressWarnings("ConstantValue")
		@Inject(method = "render", at = @At("HEAD"), cancellable = true)
		private void injectRender(Stack stack, MutableMouse mouse, float partialTicks, boolean isEditorContext, HudSize size, CallbackInfo ci) {
			if (itemStack == null || itemStack.isAir() || itemStack.getMaximumDamage() != 0 || isEditorContext)
				return;

			if (!((Object) this instanceof MainHandHudWidget) || !isEnabled(this))
				return;

			ItemStack mcStack = (ItemStack) (Object) itemStack;
			List<ItemStack> itemStacks = Arrays.asList(player().inventory.mainInventory);
			long totalAmount = dev.l3g7.griefer_utils.features.item.item_info.info_suppliers.ItemCounter.getAmount(itemStacks, mcStack);
			if (totalAmount == itemStack.getSize())
				return;

			DrawUtils.drawItem(mcStack, 0, 0, DECIMAL_FORMAT_98.format(totalAmount));
			ci.cancel();
		}

	}

}
