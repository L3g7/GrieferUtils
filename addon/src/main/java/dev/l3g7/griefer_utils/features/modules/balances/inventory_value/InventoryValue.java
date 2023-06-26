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

package dev.l3g7.griefer_utils.features.modules.balances.inventory_value;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.misc.config.Config;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.misc.gui.ItemSelectGui;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.components.EntryAddSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.labymod.settings.LabyModModuleEditorGui;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

@Singleton
public class InventoryValue extends Module {

	private static final Pattern VALUE_PATTERN = Pattern.compile("(\\d[\\d,.k]*)");
	public static String entryKey = "modules.inventory_value.entries";

	private static final BooleanSetting auto = new BooleanSetting()
		.name("Wert automatisch bestimmen")
		.description("Ob der Item-Wert automatisch bestimmt werden soll, oder ob nur Items mit einem manuell eingetragenen Wert gezählt werden sollen.")
		.config("modules.inventory_value.auto")
		.defaultValue(true)
		.icon(Material.GOLD_INGOT);

	public InventoryValue() {
		super("Inventar-Wert", "Zeigt dir an, wie viel ein Inventar wert ist.", "inventory_value", new ControlElement.IconData("griefer_utils/icons/chest.png"));
	}

	@Override
	public void fillSubSettings(List<SettingsElement> list) {
		super.fillSubSettings(list);

		list.add(auto);

		if (Config.has(entryKey)) {
			JsonObject entries = Config.get(entryKey).getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : entries.entrySet())
				list.add(new ItemDisplaySetting(ItemUtil.fromNBT(entry.getKey()), entry.getValue().getAsLong()));
		}

		list.add(new EntryAddSetting()
			.name("Item hinzufügen")
			.callback(() -> {
				if (mc().thePlayer == null) {
					displayAchievement("§e§lFehler \u26A0", "§eHinzufügen von Items ist nur Ingame möglich!");
					return;
				}

				GuiScreen guiScreen = mc().currentScreen;
				ItemSelectGui.open(stack -> mc().displayGuiScreen(new EnterItemValueGui(value -> addItem(stack, value), guiScreen, getValue(stack))));
			}));
	}

	@Override
	public String[] getKeys() {
		if (mc().currentScreen instanceof GuiInventory
			|| !(mc().currentScreen instanceof GuiContainer)
			&& !(mc().currentScreen instanceof LabyModModuleEditorGui))
			return new String[] {"Eigenes Inventar"};

		return getDefaultKeys();
	}

	@Override
	public String[] getDefaultKeys() {
		return new String[] {"Eigenes Inventar", "Geöffnetes Inventar"};
	}

	@Override
	public String[] getValues() {
		if (player() == null)
			return getDefaultValues();

		if (rawBooleanElement == null)
			createSettingElement();

		String invValue = getValue(Arrays.asList(player().inventory.mainInventory));
		if (!(mc().currentScreen instanceof GuiContainer) || mc().currentScreen instanceof GuiInventory)
			return mc().currentScreen instanceof LabyModModuleEditorGui ? new String[] {invValue, "0$"} : new String[] {invValue};

		List<Slot> slots = ((GuiContainer) mc().currentScreen).inventorySlots.inventorySlots;
		slots = slots.subList(0, slots.size() - 9 * 4);
		return new String[] {invValue, getValue(slots.stream().map(Slot::getStack).collect(Collectors.toList()))};
	}

	@Override
	public String[] getDefaultValues() {
		return new String[] {"0$", "0$"};
	}

	private void addItem(ItemStack stack, long value) {
		ItemStack is = stack.copy();
		is.stackSize = 1;

		ItemDisplaySetting setting = getSetting(stack);
		if (setting != null) {
			setting.value = value;
			onChange();
			return;
		}

		List<SettingsElement> settings = rawBooleanElement.getSubSettings().getElements();
		settings.add(settings.size() - 1, new ItemDisplaySetting(is, value));
		onChange();
	}

	private static ItemDisplaySetting getSetting(ItemStack stack) {
		if (stack == null)
			return null;

		ItemStack is = stack.copy();
		is.stackSize = 1;

		ListIterator<SettingsElement> iterator = FileProvider.getSingleton(InventoryValue.class).rawBooleanElement.getSubSettings().getElements().listIterator();
		String nbt = is.serializeNBT().toString();

		while (iterator.hasNext()) {
			SettingsElement element = iterator.next();

			if (element instanceof ItemDisplaySetting) {
				ItemDisplaySetting ids = (ItemDisplaySetting) element;
				if (nbt.equals(ids.getStack().serializeNBT().toString()))
					return ids;
			}

			if (element instanceof EntryAddSetting)
				break;
		}

		return null;
	}

	public static void onChange() {
		if (mc().currentScreen != null)
			mc().currentScreen.initGui();

		JsonObject object = new JsonObject();
		for (SettingsElement element : FileProvider.getSingleton(InventoryValue.class).rawBooleanElement.getSubSettings().getElements()) {
			if (!(element instanceof ItemDisplaySetting))
				continue;

			ItemDisplaySetting ids = (ItemDisplaySetting) element;
			object.addProperty(ids.getStack().serializeNBT().toString(), ids.value);
		}

		Config.set(entryKey, object);
		Config.save();
	}

	private static long getValue(ItemStack stack) {
		if (stack == null)
			return -1;

		List<String> lore = ItemUtil.getLore(stack);
		if (lore.size() < 3)
			return -1;

		if (!lore.get(lore.size() - 1).startsWith("§7Signiert von"))
			return -1;

		for(String string : new String[] {lore.get(lore.size() - 2), stack.getDisplayName()}) {
			Matcher matcher = VALUE_PATTERN.matcher(string.replaceAll("§.", ""));
			if (matcher.find()) {
				String result = matcher.group(1);
				if (!matcher.find()) { // Cancel if multiple numbers are found
					result = result.replaceAll("[,.]", "");
					result = result.replace("k", "000");
					try {
						return Long.parseLong(result);
					} catch (NumberFormatException ignored) {}
				} else {
					System.out.println("wat");
				}
			}
		}

		// No value was found
		return -1;
	}

	private static String getValue(List<ItemStack> itemStacks) {
		long value = 0;

		for (ItemStack itemStack : itemStacks) {
			ItemDisplaySetting ids = getSetting(itemStack);

			if (ids != null) {
				value += ids.value * itemStack.stackSize;
			} else if (auto.get()) {
				long itemValue = getValue(itemStack);
				if (itemValue != -1)
					value += itemValue * itemStack.stackSize;
			}
		}

		return Constants.DECIMAL_FORMAT_98.format(value) + "$";
	}

}