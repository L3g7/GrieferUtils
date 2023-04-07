/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package dev.l3g7.griefer_utils.settings.elements;

import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.misc.Config;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.labymod.core.LabyModCore;
import net.labymod.gui.elements.DropDownMenu;
import net.labymod.gui.elements.ModTextField;
import net.labymod.gui.elements.Scrollbar;
import net.labymod.settings.elements.DropDownElement;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.ModColor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;

// Has to be a DropDownElement, otherwise onClickDropDown won't be triggered
public class ItemSetting extends DropDownElement<ItemSetting.DummyEnum> implements ElementBuilder<ItemSetting> {

	public static ItemStack MISSING_TEXTURE = new ItemStack(Blocks.stone, 1, 10000);
	private final DropDownMenu<ItemStack> menu = new DropDownMenu<>(null, 0, 0, 0, 0);
	private final ModTextField textField = new ModTextField(-2, LabyModCore.getMinecraft().getFontRenderer(), 50, 0, 116, 20);
	private final List<ItemStack> allItems;
	private final ArrayList<ItemStack> items = Reflection.get(menu, "list");
	private final List<Consumer<ItemStack>> callbacks = new ArrayList<>();
	private final boolean sorted;
	private String configKey = null;
	private ItemStack currentValue = null;
	private ItemStack itemIcon;

	public ItemSetting(List<ItemStack> items, boolean sorted) {
		super("§cNo name set", new DropDownMenu<DummyEnum>(null, 0, 0, 0, 0) {
			@Override
			public void draw(int mouseX, int mouseY) {}
		});

		iconData = new IconData();
		itemIcon = MISSING_TEXTURE;
		textField.setEnableBackgroundDrawing(false);
		textField.setMaxStringLength(Integer.MAX_VALUE);

		this.sorted = sorted;
		this.allItems = items;
		this.items.addAll(allItems);

		menu.setEntryDrawer((o, x, y, trimmedEntry) -> {
			boolean isSelected = (menu.getY() + menu.getHeight() / 2 - 4) == y;

			if (isSelected && menu.isOpen()) // Skip the selected entry
				return;

			DrawUtils draw = new DrawUtils();
			ItemStack stack = ((ItemStack) o);

			int maxWidth = menu.getWidth() - (items.size() > 10 ? 19 : 14);
			String displayedString = mc.fontRendererObj.trimStringToWidth(stack.getDisplayName(), maxWidth);
			draw.drawString(displayedString, x + 9, y + (isSelected ? 1 : 0));

			GlStateManager.pushMatrix();
			double scale = 10 / 16d;
			double inverseScale = 1 / scale;
			double scaledX = (x - 3) * inverseScale;
			double scaledY = (y - (isSelected ? 0.5 : 2)) * inverseScale;
			GlStateManager.scale(scale, scale, scale);
			draw.drawItem(stack, scaledX, scaledY, null);
			GlStateManager.popMatrix();
		});
	}

	public void reset() {
		iconData = new IconData();
		itemIcon = MISSING_TEXTURE;
		textField.setText("");
		menu.setSelected(null);
	}

	@Override
	public void init() {
		menu.setOpen(false);
	}

	public ItemStack get() {
		return menu.getSelected();
	}

	public ItemSetting set(ItemStack stack) {
		currentValue = stack;
		menu.setSelected(stack);
		filterItems();

		callbacks.forEach(c -> c.accept(stack));
		itemIcon = stack;

		if (configKey != null) {
			Config.set(configKey, stack == null ? JsonNull.INSTANCE : new JsonPrimitive(stack.serializeNBT().toString()));
			Config.save();
		}
		return this;
	}

	public boolean isOpen() {
		return menu.isOpen();
	}

	public ItemSetting defaultValue(ItemStack defaultValue) {
		if (currentValue == null) {
			set(defaultValue);
		}
		return this;
	}

	public ItemSetting config(String configKey) {
		this.configKey = configKey;

		if (!Config.has(configKey))
			return this;

		if (Config.get(configKey).isJsonNull()) {
			set(null);
			return this;
		}

		set(ItemUtil.fromNBT(Config.get(configKey).getAsString()));

		return this;
	}

	public ItemSetting callback(Consumer<ItemStack> callback) {
		callbacks.add(callback);
		return this;
	}

	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		mouseOver = menu.isMouseOver(mouseX, mouseY);
		drawUtils().drawItem(itemIcon == null ? MISSING_TEXTURE : itemIcon, x + 5, y + 3, null);

		super.draw(x, y, maxX, maxY, mouseX, mouseY);

		drawUtils().drawRectangle(x - 1, y, x, maxY, ModColor.toRGB(120, 120, 120, 120));
		int width = Math.min(125, 165 - drawUtils().getStringWidth(getDisplayName()));
		menu.setX(maxX - width - 5);
		menu.setY(y + 3);
		menu.setWidth(width);
		menu.setHeight(maxY - y - 6);
		menu.draw(mouseX, mouseY);

		if (!textField.isFocused())
			return;

		textField.xPosition = menu.getX() + 5;
		textField.yPosition = menu.getY() + 5;
		textField.width = width - 9;
		textField.drawTextBox();
	}

	public int getEntryHeight() {
		return iconData == null ? 35 : 23;
	}

	public int getObjectWidth() {
		return menu.getWidth() + 5;
	}

	public boolean onClickDropDown(int mouseX, int mouseY, int mouseButton) {
		if (menu.onClick(mouseX, mouseY, mouseButton)) {
			textField.setFocused(menu.isOpen());
			if (menu.getSelected() != currentValue)
				set(menu.getSelected());
			textField.setText("");
			filterItems();
			return true;
		}

		textField.setFocused(menu.isOpen());

		return false;
	}

	public void mouseRelease(int mouseX, int mouseY, int mouseButton) {
		super.mouseRelease(mouseX, mouseY, mouseButton);
		menu.onRelease(mouseX, mouseY, mouseButton);
	}

	public void mouseClickMove(int mouseX, int mouseY, int mouseButton) {
		super.mouseClickMove(mouseX, mouseY, mouseButton);
		menu.onDrag(mouseX, mouseY, mouseButton);
	}

	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.textField.mouseClicked(mouseX, mouseY, 0);
	}

	public void keyTyped(char typedChar, int keyCode) {
		if (textField.textboxKeyTyped(typedChar, keyCode))
			filterItems();
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		textField.updateCursorCounter();
	}

	public void filterItems() {
		List<ItemStack> filteredItems = allItems.stream()
			.filter(i -> i.getDisplayName().toLowerCase().contains(textField.getText().toLowerCase()))
			.collect(Collectors.toList());

		if (sorted)
			filteredItems.sort(Comparator.comparing(ItemStack::getDisplayName));
		items.clear();
		items.addAll(filteredItems);

		Scrollbar scrollbar = null;

		if (items.size() > 10) {
			scrollbar = new Scrollbar(13);
			scrollbar.setSpeed(13);
			scrollbar.setListSize(items.size());
			scrollbar.setPosition(menu.getX() + menu.getWidth() - 5, menu.getY() + menu.getHeight() + 1, menu.getX() + menu.getWidth(), menu.getY() + menu.getHeight() + 1 + 130 - 1);
		}

		Reflection.set(menu, scrollbar, "scrollbar");
	}

	public void onScrollDropDown() {
		menu.onScroll();
	}

	@Override
	public ItemSetting setCallback(net.labymod.utils.Consumer<DummyEnum> consumer) {
		return this;
	}

	@Override
	public DropDownMenu<ItemStack> getDropDownMenu() {
		return menu;
	}

	@Override
	public void setChangeListener(net.labymod.utils.Consumer<DummyEnum> changeListener) {}

	public enum DummyEnum {}

}