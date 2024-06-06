/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.item_saver;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.functions.Consumer;
import dev.l3g7.griefer_utils.core.api.util.IOUtil;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageSendEvent;
import dev.l3g7.griefer_utils.features.item.item_saver.ItemSaverCategory.ItemSaver;
import dev.l3g7.griefer_utils.features.world.ItemSearch;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import static dev.l3g7.griefer_utils.core.util.ItemUtil.createItem;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

@Singleton
public class HeadSaver extends ItemSaver {

	private static final int ACCEPT_SLOT_ID = 11, PREVIEW_SLOT_ID = 13, DECLINE_SLOT_ID = 15;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("§z/kopf Vorschau")
		.description("Fragt beim Einlösen von /kopf nach einer Bestätigung und zeigt dabei eine Vorschau des Kopfes an.")
		.icon("steve");

	private final IInventory inv = new InventoryBasic(ItemSearch.marker + "§0Willst du /kopf einlösen?", false, 27);

	public HeadSaver() {
		ItemStack grayGlassPane = createItem(Blocks.stained_glass_pane, 7, "§8");

		// Fill inventory with gray glass panes
		for (int slot = 0; slot < 27; slot++)
			inv.setInventorySlotContents(slot, grayGlassPane);

		inv.setInventorySlotContents(ACCEPT_SLOT_ID, createItem(Items.dye, 10, "§aEinlösen"));
		inv.setInventorySlotContents(PREVIEW_SLOT_ID, createItem(Items.skull, 0, "§3Vorschau"));
		inv.setInventorySlotContents(DECLINE_SLOT_ID, createItem(Items.dye, 1, "§cAbbrechen"));
	}

	@EventListener
	public void onMessageSend(MessageSendEvent event) {
		if (!event.message.startsWith("/kopf "))
			return;

		String name = event.message.substring("/kopf ".length());
		TickScheduler.runAfterRenderTicks(() -> displayScreen(name), 1);
		event.cancel();
	}

	private void displayScreen(String name) {
		GuiChest chest = new GuiChest(player().inventory, inv) {

			{
				inventorySlots.windowId = -1337;
			}

			protected void handleMouseClick(Slot slot, int slotId, int btn, int type) {
				if (slot != null)
					slotId = slot.slotNumber;

				if (slotId == DECLINE_SLOT_ID)
					mc.thePlayer.closeScreenAndDropStack();

				if (slotId != ACCEPT_SLOT_ID)
					return;

				send("/kopf " + name);
				mc.thePlayer.closeScreenAndDropStack();
			}

		};

		startTextureUpdate(chest, name);
		mc().displayGuiScreen(chest);
	}

	private static void startTextureUpdate(GuiChest chest, String name) {
		String nbtFormat = "{id:\"minecraft:skull\",Count:1b,tag:{%s},Damage:3s}";
		String loreFormat = String.format(nbtFormat, "display:{Name:\"§fKopf von " + name + "\",Lore:[%s]}");

		ItemStack skull = ItemUtil.createItem(Items.skull, 3, "§fKopf von " + name);

		NBTTagCompound display = skull.getTagCompound().getCompoundTag("display");
		NBTTagList lore = new NBTTagList();
		lore.appendTag(new NBTTagString("§eTextur wird geladen..."));
		display.setTag("Lore", lore);

		chest.inventorySlots.inventorySlots.get(PREVIEW_SLOT_ID).putStack(skull);

		requestTexture(name, tex -> {
			if (tex == null) {
				skull.readFromNBT(JsonToNBT.getTagFromJson(String.format(loreFormat, "§cTextur konnte nicht geladen werden!")));
				chest.inventorySlots.inventorySlots.get(PREVIEW_SLOT_ID).putStack(skull);
				return;
			}

			String textureTag = String.format(nbtFormat, "display:{Name:\"§fKopf von " + name + "\"},SkullOwner:{Properties:{textures:[0:{Value:\"%s\"}]},Name:\"" + name + "\"}");
			skull.readFromNBT(JsonToNBT.getTagFromJson(String.format(textureTag, tex)));
			chest.inventorySlots.inventorySlots.get(PREVIEW_SLOT_ID).putStack(skull);
		});
	}

	private static void requestTexture(String name, Consumer<String> textureConsumer) {
		String url = "https://api.mojang.com/users/profiles/minecraft/" + name;
		IOUtil.read(url).asJsonObject(playerData -> {
			if (!playerData.has("id")) {
				textureConsumer.accept(null);
				return;
			}

			String uuid = playerData.get("id").getAsString();
			IOUtil.read("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid).asJsonObject(textureData -> {
				if (!textureData.has("properties")) {
					textureConsumer.accept(null);
					return;
				}

				for (JsonElement element : textureData.getAsJsonArray("properties")) {
					JsonObject property = element.getAsJsonObject();

					if (property.has("name") && property.get("name").getAsString().equals("textures")) {
						textureConsumer.accept(property.get("value").getAsString());
						return;
					}
				}
			});
		}).orElse(() -> textureConsumer.accept(null));
	}

}
