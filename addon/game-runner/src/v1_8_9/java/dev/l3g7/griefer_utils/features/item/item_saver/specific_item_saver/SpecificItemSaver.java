/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.item_saver.specific_item_saver;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.GuiModifyItemsEvent;
import dev.l3g7.griefer_utils.core.events.MouseClickEvent.LeftClickEvent;
import dev.l3g7.griefer_utils.core.events.MouseClickEvent.RightClickEvent;
import dev.l3g7.griefer_utils.core.events.WindowClickEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.core.events.render.RenderItemOverlayEvent;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.settings.types.list.ListSetting;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.world.building.AutoTool;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.ResourceLocation;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static dev.l3g7.griefer_utils.features.item.item_saver.specific_item_saver.ItemProtection.UNPROTECTED;
import static net.minecraft.network.play.client.C02PacketUseEntity.Action.ATTACK;

@Singleton
@ExclusiveTo(LABY_4)
public class SpecificItemSaver extends Feature {

	private static final ItemStack BLOCKED = ItemUtil.createItem(Blocks.stained_glass_pane, 14, "§c§lGeblockt!");

	static {ItemUtil.setLore(BLOCKED, "§cEin Item im Inventar ist im Item-Saver!");}

	private final SwitchSetting displayIcon = SwitchSetting.create()
		.name("Icon anzeigen")
		.description("Ob Items im Item-Saver mit einem Icon markiert werden sollen.")
		.icon("shield_with_sword")
		.defaultValue(true);

	private final ListSetting<ItemProtection> entries = ListSetting.create(ItemProtection.class)
		.name("Geschützte Items")
		.icon("diamond")
		.customAdd(this::addProtectedItem)
		.unpacked();

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Spezifischer Item-Saver")
		.description("Deaktiviert Klicks, Drops und Abgeben bei einstellbaren Items.\n§7(Funktioniert auch bei anderen Mods / Addons.)")
		.icon("shield_with_sword")
		.subSettings(displayIcon, HeaderSetting.create(), entries);

	// Item add logic

	/**
	 * The screen that triggered the current item add selection.
	 * If null, no selection is active.
	 */
	private GuiScreen previousScreen = null;

	private void addProtectedItem() {
		if (mc().thePlayer == null) {
			LabyBridge.labyBridge.notify("§e§lFehler ⚠", "§eHinzufügen von Items ist nur Ingame möglich!", 5000);
			return;
		}

		previousScreen = mc().currentScreen;
		display(Constants.ADDON_PREFIX + "Bitte klicke das Item an, das du hinzufügen möchtest.");
		MinecraftUtil.closeClientsideGUI();
	}

	@EventListener
	private void onAddItem(WindowClickEvent event) {
		if (previousScreen == null || event.itemStack == null)
			return;

		mc().displayGuiScreen(previousScreen);
		entries.add(new ItemProtection(event.itemStack));
		entries.notifyChange();
		previousScreen = null;
		event.cancel();
	}

	// Helpers

	public static ItemProtection getProtectionFor(ItemStack stack) {
		return get(SpecificItemSaver.class).getProtection(stack);
	}

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

		float zLevel = 299.9f;

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

	// Protections

	@EventListener
	private void denyRightClick(RightClickEvent event) {
		if (getProtection(heldItem()).deniesRightClick())
			event.cancel();
	}

	@EventListener
	private void denyLeftClick(LeftClickEvent event) {
		if (getProtection(heldItem()).deniesLeftClick())
			event.cancel();
	}

	/**
	 * Protects against GUIs that might remove an item.
	 */
	@EventListener
	private void denyGuiActions(GuiModifyItemsEvent event) {
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
	private void denyWindowClick(WindowClickEvent event) {
		if (event.itemStack == BLOCKED) {
			event.cancel();
			return;
		}

		ItemProtection protection = getProtection(event.itemStack);
		if (protection.deniesItemPickup() && !isAuctionHouse(event.windowId)) {
			if (event.mode == 0 || event.mode == 6)
				event.cancel();
		}

		if (protection.deniesDrop() && event.mode == 4)
			event.cancel();
	}

	private boolean isAuctionHouse(int windowId) {
		if (!(mc().currentScreen instanceof GuiChest gc) || gc.inventorySlots.windowId != windowId)
			return false;

		IInventory lowerInv = Reflection.get(gc, "lowerChestInventory");
		String title = lowerInv.getDisplayName().getFormattedText();
		if (!title.startsWith("§6"))
			return false;

		return title.contains("Auktion") || title.startsWith("§6Filter auswählen");
	}

	@EventListener
	private void denyDigging(PacketSendEvent<C07PacketPlayerDigging> event) {
		switch (event.packet.getStatus()) {
			case START_DESTROY_BLOCK -> {
				if (getProtection(heldItem()).deniesLeftClick())
					event.cancel();
			}
			case DROP_ALL_ITEMS, DROP_ITEM -> {
				if (getProtection(heldItem()).deniesDrop())
					event.cancel();
			}
		}
	}

	@EventListener
	private void denyUseEntity(PacketSendEvent<C02PacketUseEntity> event) {
		var protection = getProtection(heldItem());
		if (event.packet.getAction() == ATTACK ? protection.deniesLeftClick() : protection.deniesRightClick())
			event.cancel();
	}

	@EventListener
	private void denyPlaceBlock(PacketSendEvent<C08PacketPlayerBlockPlacement> event) {
		if (getProtection(heldItem()).deniesRightClick())
			event.cancel();
	}

}
