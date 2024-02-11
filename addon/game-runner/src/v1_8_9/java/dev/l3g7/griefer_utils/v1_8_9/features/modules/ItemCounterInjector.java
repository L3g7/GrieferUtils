/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.modules;

import dev.l3g7.griefer_utils.laby4.settings.types.SwitchSettingImpl;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.features.item.item_info.info_suppliers.ItemCounter;
import dev.l3g7.griefer_utils.v1_8_9.misc.gui.elements.laby_polyfills.DrawUtils;
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
import net.labymod.core.client.gui.hud.hudwidget.item.equipment.MainHandHudWidget;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.l3g7.griefer_utils.api.misc.Constants.DECIMAL_FORMAT_98;
import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.player;

public class ItemCounterInjector {

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
		switchSetting.addSetting(ItemCounter.ignoreDamage);
		switchSetting.addSetting(ItemCounter.ignoreEnchants);
		switchSetting.addSetting(ItemCounter.ignoreLore);

		switchSetting.create(switchSetting);

		HudWidget<?> hudWidget = (HudWidget<?>) widget;
		hudWidget.getSettings().add(switchSetting);

		settings.put(widget, switchSetting);
	}

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

	@Mixin(value = ItemCounterHudWidget.CountingItem.class, remap = false)
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
			int totalAmount = ItemCounter.getAmount(itemStacks, stack);
			if (totalAmount == count)
				return;

			itemCount = totalAmount;
			renderableCount = RenderableComponent.of(Component.text(DECIMAL_FORMAT_98.format(totalAmount)));
			ci.cancel();
		}

	}

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
			int totalAmount = ItemCounter.getAmount(itemStacks, mcStack);
			if (totalAmount == itemStack.getSize())
				return;

			DrawUtils.drawItem(mcStack, 0, 0, DECIMAL_FORMAT_98.format(totalAmount));
			ci.cancel();
		}

	}

}
