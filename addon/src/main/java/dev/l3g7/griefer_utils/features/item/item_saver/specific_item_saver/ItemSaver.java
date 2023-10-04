/*
 * This file is part of GrieferUtils https://github.com/L3g7/GrieferUtils.
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 the "License";
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

package dev.l3g7.griefer_utils.features.item.item_saver.specific_item_saver;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.misc.config.Config;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.events.GuiModifyItemsEvent;
import dev.l3g7.griefer_utils.event.events.MouseClickEvent;
import dev.l3g7.griefer_utils.event.events.MouseClickEvent.LeftClickEvent;
import dev.l3g7.griefer_utils.event.events.MouseClickEvent.RightClickEvent;
import dev.l3g7.griefer_utils.event.events.WindowClickEvent;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.event.events.render.RenderItemOverlayEvent;
import dev.l3g7.griefer_utils.features.item.AutoTool;
import dev.l3g7.griefer_utils.features.item.item_saver.ItemSaverCategory;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.components.EntryAddSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.C07PacketPlayerDigging;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;
import static net.minecraft.network.play.client.C07PacketPlayerDigging.Action.DROP_ALL_ITEMS;
import static net.minecraft.network.play.client.C07PacketPlayerDigging.Action.DROP_ITEM;

@Singleton
public class ItemSaver extends ItemSaverCategory.ItemSaver {

	private static final String BONZE_NBT = "{id:\"minecraft:diamond_sword\",Count:1b,tag:{ench:[0:{lvl:21s,id:16s},1:{lvl:3s,id:34s},2:{lvl:2s,id:20s},3:{lvl:5s,id:61s},4:{lvl:21s,id:21s}],display:{Name:\"§6Klinge von GrafBonze\"}},Damage:0s}";
	private static final String BIRTH_NBT = "{id:\"minecraft:diamond_sword\",Count:1b,tag:{ench:[0:{lvl:21s,id:16s},1:{lvl:2s,id:20s},2:{lvl:5s,id:61s},3:{lvl:21s,id:21s}],display:{Name:\"§4B§aI§3R§2T§eH §4§lKlinge\"}},Damage:0s}";
	private static final ItemStack blockedIndicator = ItemUtil.createItem(Blocks.stained_glass_pane, 14, "§c§lGeblockt!");;

	private static String entryKey;
	private static String iconKey;
	private static GuiScreen previousScreen = null;

	private static final BooleanSetting displayIcon = new BooleanSetting()
		.name("Icon anzeigen")
		.description("Ob Items im ItemSaver mit einem Icon markiert werden sollen.")
		.icon("shield_with_sword")
		.defaultValue(true)
		.callback(b -> {
			Config.set(iconKey, new JsonPrimitive(b));
			Config.save();
		});

	private static final EntryAddSetting newEntrySetting = new EntryAddSetting()
		.name("Item hinzufügen")
		.callback(() -> {
			if (mc().thePlayer == null) {
				displayAchievement("§e§lFehler \u26A0", "§eHinzufügen von Items ist nur Ingame möglich!");
				return;
			}

			previousScreen = mc().currentScreen;
			display(Constants.ADDON_PREFIX + "Bitte klicke das Item an, das du hinzufügen möchtest.");
			mc().displayGuiScreen(null);
		});

	@MainElement(configureSubSettings = false)
	static final BooleanSetting enabled = new BooleanSetting()
		.name("Spezifischer Item-Saver")
		.description("Deaktiviert Klicks und Dropping bei einstellbaren Items.")
		.icon("shield_with_sword")
		.subSettings(displayIcon, new HeaderSetting(), newEntrySetting);

	public static ItemDisplaySetting getSetting(ItemStack stack) {
		if (stack == null || !FileProvider.getSingleton(ItemSaver.class).isEnabled())
			return null;

		for (SettingsElement element : enabled.getSubSettings().getElements()) {
			if (!(element instanceof ItemDisplaySetting))
				continue;

			ItemDisplaySetting setting = (ItemDisplaySetting) element;

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

		NBTTagCompound cleanedStackNBT = (NBTTagCompound) stackNBT.copy();
		cleanedStackNBT.removeTag("display");
		cleanedStackNBT.removeTag("RepairCost");

		return cleanedStackNBT.equals(settingNBT);
	}

	@Override
	public void init() {
		super.init();

		ItemUtil.setLore(blockedIndicator, "§cEin Item im Inventar ist im Item-Saver!");

		iconKey = getConfigKey() + ".display_icon";
		if (Config.has(iconKey))
			displayIcon.set(Config.get(iconKey).getAsBoolean());

		entryKey = getConfigKey() + ".entries";

		if (!Config.has(entryKey)) {
			addItem(ItemUtil.fromNBT(BONZE_NBT));
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

				List<SettingsElement> settings = enabled.getSubSettings().getElements();
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

		float zLevel = Reflection.get(drawUtils(), "zLevel");
		zLevel += 500;

		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		GlStateManager.color(1, 1, 1, 1);
		drawUtils().bindTexture("griefer_utils/icons/shield_with_sword.png");

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
	private void onRightClick(MouseClickEvent.RightClickEvent event) {
		onMouse(event);
	}

		@EventListener
	private void onLeftClick(MouseClickEvent.LeftClickEvent event) {
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
			for (int i : new int[] {10, 13, 16}) {
				ItemStack sellingItem = event.getItem(i);
				if (sellingItem == null || AutoTool.isTool(sellingItem))
					continue;

				if (savedStackWithSaveItemExists(event.getInventory(), sellingItem)) {
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
				for (int i : new int[] {11, 13, 15}) {
					if (savedStackWithSaveItemExists(event.getInventory(), event.getItem(i))) {
						isBlocked = true;
						break;
					}
				}
			}

			if (!isBlocked)
				return;

			for (int i : new int[] {11, 13, 15})
				event.setItem(i, blockedIndicator);

			return;
		}

		if (event.getTitle().startsWith("§6Bauanleitung") || event.getTitle().startsWith("§6Vanilla Bauanleitung")) {
			for (int i = 0; i < 9; i++) {
				int slotId = (i / 3) * 9 + 10 + i % 3;
				if (!savedStackWithSaveItemExists(event.getInventory(), event.getItem(slotId)))
					continue;

				for (int j = 46; j < 54; j++) {
					if (event.getItem(j).getItem() == Items.skull)
						event.setItem(j, blockedIndicator);
				}
				return;
			}
		}
	}

	private boolean savedStackWithSaveItemExists(List<ItemStack> itemStacks, ItemStack comparison) {
		if (comparison == null)
			return false;

		for (ItemStack itemStack : itemStacks)
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

	@EventListener
	private void onAddItem(WindowClickEvent event) {
		if (previousScreen == null || event.itemStack == null)
			return;

		mc().displayGuiScreen(previousScreen);
		ItemSaver.addItem(event.itemStack);
		previousScreen = null;
		event.cancel();
	}

	@EventListener
	private void onPacketSend(PacketEvent.PacketSendEvent<C07PacketPlayerDigging> event) {
		C07PacketPlayerDigging.Action action = event.packet.getStatus();
		if (action != DROP_ITEM && action != DROP_ALL_ITEMS)
			return;

		ItemDisplaySetting setting = getSetting(player().getHeldItem());
		if (setting != null && setting.drop.get())
			event.cancel();
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

		ListIterator<SettingsElement> iterator = enabled.getSubSettings().getElements().listIterator();
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
		for (SettingsElement element : enabled.getSubSettings().getElements()) {
			if (!(element instanceof ItemDisplaySetting))
				continue;

			ItemDisplaySetting itemDisplaySetting = (ItemDisplaySetting) element;

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

}
