/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.item_saver.specific_item_saver.laby3;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.events.GuiModifyItemsEvent;
import dev.l3g7.griefer_utils.core.events.MouseClickEvent;
import dev.l3g7.griefer_utils.core.events.MouseClickEvent.LeftClickEvent;
import dev.l3g7.griefer_utils.core.events.MouseClickEvent.RightClickEvent;
import dev.l3g7.griefer_utils.core.events.WindowClickEvent;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnStartupComplete;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent;
import dev.l3g7.griefer_utils.core.events.render.RenderItemOverlayEvent;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.item.AutoTool;
import dev.l3g7.griefer_utils.features.item.item_saver.ItemSaverCategory;
import dev.l3g7.griefer_utils.features.item.item_saver.specific_item_saver.TempItemSaverBridge;
import net.labymod.core.LabyModCore;
import net.labymod.core.WorldRendererAdapter;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;
import static net.minecraft.network.play.client.C07PacketPlayerDigging.Action.*;

@Bridge
@Singleton
@ExclusiveTo(LABY_3)
public class ItemSaver extends ItemSaverCategory.ItemSaver implements TempItemSaverBridge {

	private static final String BONZE_NBT = "{id:\"minecraft:diamond_sword\",Count:1b,tag:{ench:[0:{lvl:21s,id:16s},1:{lvl:3s,id:34s},2:{lvl:2s,id:20s},3:{lvl:5s,id:61s},4:{lvl:21s,id:21s}],display:{Name:\"§6Klinge von GrafBonze\"}},Damage:0s}";
	private static final String BONZE24_NBT = "{id:\"minecraft:diamond_sword\",Count:1b,tag:{ench:[0:{lvl:24s,id:16s},1:{lvl:3s,id:34s},2:{lvl:2s,id:20s},3:{lvl:5s,id:61s},4:{lvl:24s,id:21s}],display:{Name:\"§6Klinge von GrafBonze\"}},Damage:0s}";
	private static final String BIRTH_NBT = "{id:\"minecraft:diamond_sword\",Count:1b,tag:{ench:[0:{lvl:21s,id:16s},1:{lvl:2s,id:20s},2:{lvl:5s,id:61s},3:{lvl:21s,id:21s}],display:{Name:\"§4B§aI§3R§2T§eH §4§lKlinge\"}},Damage:0s}";
	private static final ItemStack blockedIndicator = ItemUtil.createItem(Blocks.stained_glass_pane, 14, "§c§lGeblockt!");

	private static String entryKey;
	private static String iconKey;
	private static GuiScreen previousScreen = null;

	private static final SwitchSetting displayIcon = SwitchSetting.create()
		.name("Icon anzeigen")
		.description("Ob Items im ItemSaver mit einem Icon markiert werden sollen.")
		.icon("shield_with_sword")
		.defaultValue(true)
		.callback(b -> {
			Config.set(iconKey, new JsonPrimitive(b));
			Config.save();
		});

	private static final EntryAddSetting newEntrySetting = new EntryAddSetting("Item hinzufügen")
		.callback(() -> {
			if (mc().thePlayer == null) {
				LabyBridge.labyBridge.notify("§e§lFehler ⚠", "§eHinzufügen von Items ist nur Ingame möglich!", 5000);
				return;
			}

			previousScreen = mc().currentScreen;
			display(Constants.ADDON_PREFIX + "Bitte klicke das Item an, das du hinzufügen möchtest.");
			mc().displayGuiScreen(null);
		});

	@MainElement(configureSubSettings = false)
	static final SwitchSetting enabled = SwitchSetting.create()
		.name("Spezifischer Item-Saver")
		.description("Deaktiviert Klicks, Dropping und Abgeben bei einstellbaren Items.\n§7(Funktioniert auch bei anderen Mods / Addons.)")
		.icon("shield_with_sword")
		.subSettings(displayIcon, HeaderSetting.create(), newEntrySetting);

	public static ItemDisplaySetting getSetting(ItemStack stack) {
		if (stack == null || !FileProvider.getSingleton(ItemSaver.class).isEnabled())
			return null;

		for (SettingsElement element : ((SettingsElement) enabled).getSubSettings().getElements()) {
			if (!(element instanceof ItemDisplaySetting setting))
				continue;

			ItemStack settingStack = setting.getStack();
			if (settingStack.getItem() != stack.getItem())
				continue;

			if (!stack.isItemStackDamageable() && settingStack.getMetadata() != stack.getMetadata())
				continue;

			if (areTagsEqual(stack.getTagCompound(), settingStack.getTagCompound()))
				return setting;
		}

		return null;
	}

	private static boolean areTagsEqual(NBTTagCompound stackNBT, NBTTagCompound settingNBT) {
		if (stackNBT == null)
			return settingNBT == null;

		stackNBT = ItemUtil.safeCopy(stackNBT);
		NBTTagCompound cleanedStackNBT = new NBTTagCompound();
		for (String s : stackNBT.getKeySet()) {
			if (s.equals("display") || s.equals("RepairCost"))
				continue;

			NBTBase tag = stackNBT.getTag(s);
			cleanedStackNBT.setTag(s, tag == null ? null : tag.copy());
		}

		return cleanedStackNBT.equals(settingNBT);
	}

	@OnStartupComplete
	public void initialize() {

		ItemUtil.setLore(blockedIndicator, "§cEin Item im Inventar ist im Item-Saver!");

		iconKey = getConfigKey() + ".display_icon";
		if (Config.has(iconKey))
			displayIcon.set(Config.get(iconKey).getAsBoolean());

		entryKey = getConfigKey() + ".entries";

		if (!Config.has(entryKey)) {
			addItem(ItemUtil.fromNBT(BONZE_NBT));
			addItem(ItemUtil.fromNBT(BONZE24_NBT));
			addItem(ItemUtil.fromNBT(BIRTH_NBT));
			return;
		}

		JsonObject entries = Config.get(entryKey).getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : entries.entrySet()) {
			try {
				ItemStack stack = ItemUtil.fromNBT(entry.getKey());
				JsonObject data = entry.getValue().getAsJsonObject();

				if (stack == null)
					continue;

				ItemDisplaySetting setting = new ItemDisplaySetting(stack);
				setting.name.set(data.get("name").getAsString());
				setting.extremeDrop.set(data.get("extreme_drop").getAsBoolean());
				setting.drop.set(data.get("drop").getAsBoolean());
				setting.leftclick.set(data.get("leftclick").getAsBoolean());
				setting.rightclick.set(data.get("rightclick").getAsBoolean());

				List<SettingsElement> settings = ((SettingsElement) enabled).getSubSettings().getElements();
				settings.add(settings.size() - 1, setting);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}

	@EventListener
	public void onGuiDraw(RenderItemOverlayEvent event) {
		if (!displayIcon.get() || getSetting(event.stack) == null)
			return;

		float zLevel = DrawUtils.zLevel;
		zLevel += 500;

		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		GlStateManager.color(1, 1, 1, 1);
		DrawUtils.bindTexture("griefer_utils/icons/shield_with_sword.png");

		float x = event.x - 0.5f;
		float y = event.y;
		float height = 1, width = 1f;
		float scale = 10 / width;
		GlStateManager.scale(scale, scale, 1);
		x *= 1 / scale;
		y *= 1 / scale;

		WorldRendererAdapter worldrenderer = LabyModCore.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(x, y + height, zLevel).tex(0, height).endVertex();
		worldrenderer.pos(x + width, y + height, zLevel).tex(width, height).endVertex();
		worldrenderer.pos(x + width, y, zLevel).tex(width, 0).endVertex();
		worldrenderer.pos(x, y, zLevel).tex(0, 0).endVertex();
		Tessellator.getInstance().draw();
		GlStateManager.popMatrix();
	}

	@EventListener
	private void onRightClick(RightClickEvent event) {
		onMouse(event);
	}

	@EventListener
	private void onLeftClick(LeftClickEvent event) {
		onMouse(event);
	}

	private void onMouse(MouseClickEvent event) {
		if (player() == null)
			return;

		InventoryPlayer inv = player().inventory;
		if (inv.getCurrentItem() == null)
			return;

		ItemDisplaySetting setting = getSetting(player().getHeldItem());
		if (setting == null)
			return;

		if ((event instanceof LeftClickEvent && setting.leftclick.get()) || (event instanceof RightClickEvent && setting.rightclick.get()))
			event.cancel();
	}

	@EventListener
	private void onGuiSetItems(GuiModifyItemsEvent event) {
		if (event.getTitle().startsWith("§6Adventure-Jobs")) {
			for (int i : new int[]{10, 13, 16}) {
				ItemStack sellingItem = event.getItem(i);
				if (sellingItem == null || AutoTool.isTool(sellingItem))
					continue;

				if (savedStackWithSameItemExists(sellingItem)) {
					event.setItem(i, blockedIndicator);
					return;
				}
			}

			return;
		}

		if (event.getTitle().startsWith("§6Orbs - Verkauf ")) {
			ItemStack firstStack = event.getItem(11);
			boolean isBlocked = firstStack == blockedIndicator;
			if (!isBlocked) {
				for (int i : new int[]{11, 13, 15}) {
					if (savedStackWithSameItemExists(event.getItem(i))) {
						isBlocked = true;
						break;
					}
				}
			}

			if (!isBlocked)
				return;

			for (int i : new int[]{11, 13, 15})
				event.setItem(i, blockedIndicator);

			return;
		}

		if (event.getTitle().startsWith("§6Bauanleitung") || event.getTitle().startsWith("§6Vanilla Bauanleitung")) {
			for (int i = 0; i < 9; i++) {
				int slotId = (i / 3) * 9 + 10 + i % 3;
				if (!savedStackWithSameItemExists(event.getItem(slotId)))
					continue;

				for (int j = 46; j < 54; j++) {
					if (event.getItem(j) != null && event.getItem(j).getItem() == Items.skull)
						event.setItem(j, blockedIndicator);
				}
				return;
			}
		}
	}

	private boolean savedStackWithSameItemExists(ItemStack comparison) {
		if (comparison == null)
			return false;

		for (ItemStack itemStack : player().inventory.mainInventory)
			if (comparison.isItemEqual(itemStack) && getSetting(itemStack) != null)
				return true;

		return false;
	}

	@EventListener
	private void onWindowClick(WindowClickEvent event) {
		if (event.itemStack == blockedIndicator) {
			event.cancel();
			return;
		}

		ItemDisplaySetting setting = getSetting(event.itemStack);
		if (setting == null)
			return;

		if (setting.extremeDrop.get()) {
			if (event.mode == 0 || event.mode == 6)
				event.cancel();
		}

		if (setting.drop.get() && event.mode == 4)
			event.cancel();
	}

	@EventListener(triggerWhenDisabled = true)
	private void onAddItem(WindowClickEvent event) {
		if (previousScreen == null || event.itemStack == null)
			return;

		mc().displayGuiScreen(previousScreen);
		ItemSaver.addItem(event.itemStack);
		previousScreen = null;
		event.cancel();
	}

	@EventListener
	private void onPacketDigging(PacketEvent.PacketSendEvent<Packet<?>> event) {
		if (cancel(event.packet))
			event.cancel();
	}

	private boolean cancel(Packet<?> packet) {
		if (player() == null)
			return false;

		ItemDisplaySetting setting = getSetting(player().getHeldItem());
		if (setting == null)
			return false;

		if (packet instanceof C07PacketPlayerDigging) {
			C07PacketPlayerDigging.Action action = ((C07PacketPlayerDigging) packet).getStatus();

			if (setting.drop.get() && (action == DROP_ITEM || action == DROP_ALL_ITEMS))
				return true;
			else
				return setting.leftclick.get() && action == START_DESTROY_BLOCK;
		}

		if (packet instanceof C02PacketUseEntity) {
			Action action = ((C02PacketUseEntity) packet).getAction();
			return (action == Action.ATTACK ? setting.leftclick : setting.rightclick).get();
		}

		if (packet instanceof C08PacketPlayerBlockPlacement p)
			return (setting = getSetting(p.getStack())) != null && setting.rightclick.get();

		return false;
	}

	private static void addItem(ItemStack stack) {
		ItemStack is = stack.copy();

		if (is.isItemStackDamageable())
			is.setItemDamage(0);

		String name = is.getDisplayName();

		if (is.hasTagCompound()) {
			is.getTagCompound().removeTag("display");
			is.getTagCompound().removeTag("RepairCost");
		}

		is.stackSize = 1;

		ListIterator<SettingsElement> iterator = ((SettingsElement) enabled).getSubSettings().getElements().listIterator();
		String nbt = ItemUtil.serializeNBT(is);

		while (iterator.hasNext()) {
			SettingsElement element = iterator.next();

			if (element instanceof ItemDisplaySetting)
				if (nbt.equals(ItemUtil.serializeNBT(((ItemDisplaySetting) element).getStack())))
					return;

			if (element instanceof EntryAddSetting)
				break;
		}

		iterator.previous();
		ItemDisplaySetting setting = new ItemDisplaySetting(is);
		setting.name.set(name);
		iterator.add(setting);
		onChange();
	}

	public static void onChange() {
		if (mc().currentScreen != null)
			mc().currentScreen.initGui();

		JsonObject object = new JsonObject();
		for (SettingsElement element : ((SettingsElement) enabled).getSubSettings().getElements()) {
			if (!(element instanceof ItemDisplaySetting itemDisplaySetting))
				continue;

			JsonObject entry = new JsonObject();
			entry.addProperty("name", itemDisplaySetting.name.get());
			entry.addProperty("drop", itemDisplaySetting.drop.get());
			entry.addProperty("extreme_drop", itemDisplaySetting.extremeDrop.get());
			entry.addProperty("leftclick", itemDisplaySetting.leftclick.get());
			entry.addProperty("rightclick", itemDisplaySetting.rightclick.get());

			object.add(ItemUtil.serializeNBT(itemDisplaySetting.getStack()), entry);
		}

		Config.set(entryKey, object);
		Config.save();
	}

	@Override
	public boolean isProtected(ItemStack itemStack) {
		return getSetting(itemStack) != null;
	}

	@Override
	public boolean isProtectedAgainstLeftClick(ItemStack itemStack) {
		ItemDisplaySetting ids = getSetting(itemStack);
		return ids != null && ids.leftclick.get();
	}

	@Override
	public boolean isProtectedAgainstItemPickup(ItemStack itemStack) {
		ItemDisplaySetting ids = getSetting(itemStack);
		return ids != null && ids.extremeDrop.get();
	}

}
