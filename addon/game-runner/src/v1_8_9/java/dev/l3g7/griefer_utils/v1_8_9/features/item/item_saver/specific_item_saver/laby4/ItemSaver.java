/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.item.item_saver.specific_item_saver.laby4;

import dev.l3g7.griefer_utils.api.bridges.Bridge;
import dev.l3g7.griefer_utils.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.GuiModifyItemsEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.MouseClickEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.WindowClickEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.render.RenderItemOverlayEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.item.AutoTool;
import dev.l3g7.griefer_utils.v1_8_9.features.item.item_saver.ItemSaverCategory;
import dev.l3g7.griefer_utils.v1_8_9.features.item.item_saver.specific_item_saver.TempItemSaverBridge;
import dev.l3g7.griefer_utils.v1_8_9.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.ResourceLocation;

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.v1_8_9.features.item.item_saver.specific_item_saver.laby4.ItemProtection.ProtectionType.*;
import static dev.l3g7.griefer_utils.v1_8_9.features.item.item_saver.specific_item_saver.laby4.ItemProtection.UNPROTECTED;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.heldItem;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.inventory;
import static net.minecraft.network.play.client.C02PacketUseEntity.Action.ATTACK;

@Bridge
@Singleton
@ExclusiveTo(LABY_4)
public class ItemSaver extends ItemSaverCategory.ItemSaver implements TempItemSaverBridge { // FIXME: test, test dependant features

	private static final ItemStack BLOCKED = ItemUtil.createItem(Blocks.stained_glass_pane, 14, "§c§lGeblockt!");

	static {ItemUtil.setLore(BLOCKED, "§cEin Item im Inventar ist im Item-Saver!");}

	private final SwitchSetting displayIcon = SwitchSetting.create()
		.name("Icon anzeigen")
		.description("Ob Items im ItemSaver mit einem Icon markiert werden sollen.")
		.icon("shield_with_sword")
		.defaultValue(true);

	private final ItemProtectionListSetting entries = new ItemProtectionListSetting()
		.name("Geschützte Items")
		.disableSubsettingConfig()
		.icon(Items.diamond);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Spezifischer Item-Saver")
		.description("Deaktiviert Klicks, Dropping und Abgeben bei einstellbaren Items.\n§7(Funktioniert auch bei anderen Mods / Addons.)")
		.icon("shield_with_sword")
		.subSettings(displayIcon, HeaderSetting.create(), entries);

	private ItemProtection getProtection(ItemStack stack) {
		if (stack == null || !isEnabled())
			return UNPROTECTED;

		for (ItemProtection protection : entries.get())
			if (protection.appliesTo(stack))
				return protection;

		return UNPROTECTED;
	}

	private boolean isProtectedAndInInventory(ItemStack comparison) {
		if (comparison == null)
			return false;

		for (ItemStack itemStack : inventory().mainInventory)
			if (comparison.isItemEqual(itemStack) && getProtection(itemStack).isProtected())
				return true;

		return false;
	}

	/**
	 * Renders a shield on protected items.
	 */
	@EventListener
	private void onGuiDraw(RenderItemOverlayEvent event) {
		if (!displayIcon.get() || !getProtection(event.stack).isProtected())
			return;

		float zLevel = DrawUtils.zLevel;
		zLevel += 500;

		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		GlStateManager.color(1, 1, 1, 1);
		DrawUtils.bindTexture(new ResourceLocation("griefer_utils", "icons/shield_with_sword.png"));

		float x = event.x - 0.5f;
		float y = event.y;
		float height = 1, width = 1f;
		float scale = 10 / width;
		GlStateManager.scale(scale, scale, 1);
		x *= 1 / scale;
		y *= 1 / scale;

		WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(x, y + height, zLevel).tex(0, height).endVertex();
		worldrenderer.pos(x + width, y + height, zLevel).tex(width, height).endVertex();
		worldrenderer.pos(x + width, y, zLevel).tex(width, 0).endVertex();
		worldrenderer.pos(x, y, zLevel).tex(0, 0).endVertex();
		Tessellator.getInstance().draw();
		GlStateManager.popMatrix();
	}

	@EventListener
	private void onRightClick(MouseClickEvent.RightClickEvent event) {
		if (getProtection(heldItem()).isProtectedAgainst(RIGHT_CLICK))
			event.cancel();
	}

	@EventListener
	private void onLeftClick(MouseClickEvent.LeftClickEvent event) {
		if (getProtection(heldItem()).isProtectedAgainst(LEFT_CLICK))
			event.cancel();
	}

	/**
	 * Protects against GUIs that might remove an item.
	 */
	@EventListener
	private void onGuiSetItems(GuiModifyItemsEvent event) {
		// Protect against adventure trader
		if (event.getTitle().startsWith("§6Adventure-Jobs")) {
			for (int i : new int[]{10, 13, 16}) {
				ItemStack sellingItem = event.getItem(i);
				if (sellingItem == null || AutoTool.isTool(sellingItem))
					continue;

				if (isProtectedAndInInventory(sellingItem)) {
					event.setItem(i, BLOCKED);
					return;
				}
			}

			return;
		}

		// Protect against orbs trader
		if (event.getTitle().startsWith("§6Orbs - Verkauf ")) {
			ItemStack firstStack = event.getItem(11);
			boolean isBlocked = firstStack == BLOCKED;
			if (!isBlocked) {
				for (int i : new int[]{11, 13, 15}) {
					if (isProtectedAndInInventory(event.getItem(i))) {
						isBlocked = true;
						break;
					}
				}
			}

			if (!isBlocked)
				return;

			for (int i : new int[]{11, 13, 15})
				event.setItem(i, BLOCKED);

			return;
		}

		// Protect against /recipes
		if (event.getTitle().startsWith("§6Bauanleitung") || event.getTitle().startsWith("§6Vanilla Bauanleitung")) {
			for (int i = 0; i < 9; i++) {
				int slotId = i / 3 * 9 + 10 + i % 3;
				if (!isProtectedAndInInventory(event.getItem(slotId)))
					continue;

				for (int j = 46; j < 54; j++) {
					if (event.getItem(j) != null && event.getItem(j).getItem() == Items.skull)
						event.setItem(j, BLOCKED);
				}
				return;
			}
		}
	}

	@EventListener
	private void onWindowClick(WindowClickEvent event) {
		if (event.itemStack == BLOCKED) {
			event.cancel();
			return;
		}

		ItemProtection protection = getProtection(event.itemStack);
		if (protection.isProtectedAgainst(ITEM_PICKUP)) {
			if (event.mode == 0 || event.mode == 6)
				event.cancel();
		}

		if (protection.isProtectedAgainst(DROP) && event.mode == 4)
			event.cancel();
	}


	@EventListener
	private void onPacketDigging(PacketSendEvent<C07PacketPlayerDigging> event) {
		switch (event.packet.getStatus()) {
			case START_DESTROY_BLOCK -> {
				if (getProtection(heldItem()).isProtectedAgainst(LEFT_CLICK))
					event.cancel();
			}
			case DROP_ALL_ITEMS, DROP_ITEM -> {
				if (getProtection(heldItem()).isProtectedAgainst(DROP))
					event.cancel();
			}
		}
	}

	@EventListener
	private void onPacketUseEntity(PacketSendEvent<C02PacketUseEntity> event) {
		if (getProtection(heldItem()).isProtectedAgainst(event.packet.getAction() == ATTACK ? LEFT_CLICK : RIGHT_CLICK))
			event.cancel();
	}

	@EventListener
	private void onPacketPlaceBlock(PacketSendEvent<C08PacketPlayerBlockPlacement> event) {
		if (getProtection(heldItem()).isProtectedAgainst(RIGHT_CLICK))
			event.cancel();
	}

	@Override
	public boolean isProtected(ItemStack itemStack) {
		return getProtection(itemStack).isProtected();
	}

	@Override
	public boolean isProtectedAgainstLeftClick(ItemStack itemStack) {
		return getProtection(itemStack).isProtectedAgainst(LEFT_CLICK);
	}

	@Override
	public boolean isProtectedAgainstItemPickup(ItemStack itemStack) {
		return getProtection(itemStack).isProtectedAgainst(ITEM_PICKUP);
	}

}
