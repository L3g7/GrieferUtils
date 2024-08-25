/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.settings.types;

import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.misc.Citybuild;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.labymod.laby3.settings.Laby3Setting;
import dev.l3g7.griefer_utils.core.settings.types.CitybuildSetting;
import net.labymod.core.LabyModCore;
import net.labymod.gui.elements.DropDownMenu;
import net.labymod.gui.elements.ModTextField;
import net.labymod.gui.elements.Scrollbar;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.DropDownElement;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.ModColor;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.core.api.misc.Citybuild.ANY;
import static dev.l3g7.griefer_utils.core.util.ItemUtil.createItem;
import static net.minecraft.init.Blocks.*;

public class CitybuildSettingImpl extends DropDownElement<CitybuildSettingImpl.DummyEnum> implements Laby3Setting<CitybuildSetting, Citybuild>, CitybuildSetting {

	// TODO: cleanup whatever this is
	private final ExtendedStorage<Citybuild> storage = new ExtendedStorage<>(e -> {
		String name = e.name();
		if (e == ANY) {
			name = "Egal";
		} else if (!e.name().startsWith("CB")) {
			StringBuilder sb = new StringBuilder(name.toLowerCase());
			sb.setCharAt(0, e.name().charAt(0));
			name = sb.toString();
		}

		return new JsonPrimitive(name);
	}, e -> Citybuild.getCitybuild(e.getAsString()), ANY);

	@Override
	public ExtendedStorage<Citybuild> getStorage() {
		return storage;
	}

	public static final List<Pair<ItemStack, Citybuild>> CB_ITEMS = new ArrayList<>();

	static {
		CB_ITEMS.add(new Pair<>(createItem(Items.nether_star, 0, "Egal"), ANY));

		Block[] blocks = new Block[]{diamond_block, emerald_block, gold_block, redstone_block, lapis_block, coal_block, emerald_ore, redstone_ore, diamond_ore, gold_ore, iron_ore, coal_ore, lapis_ore, bedrock, gravel, obsidian, barrier, iron_block, barrier, prismarine, mossy_cobblestone, brick_block};
		for (int i = 0; i < blocks.length; i++)
			CB_ITEMS.add(new Pair<>(createItem(blocks[i], 0, "CB" + (i + 1)), Citybuild.valueOf("CB" + (i + 1))));

		CB_ITEMS.set(17, new Pair<>(createItem(stone, 6, "CB17"), Citybuild.CB17));
		CB_ITEMS.set(19, new Pair<>(createItem(prismarine, 2, "CB19"), Citybuild.CB19));
		CB_ITEMS.add(new Pair<>(createItem(sapling, 5, "Nature"), Citybuild.NATURE));
		CB_ITEMS.add(new Pair<>(createItem(sapling, 3, "Extreme"), Citybuild.EXTREME));
		CB_ITEMS.add(new Pair<>(createItem(netherrack, 0, "CBE"), Citybuild.CBE));
		CB_ITEMS.add(new Pair<>(createItem(Items.water_bucket, 0, "Wasser"), Citybuild.WATER));
		CB_ITEMS.add(new Pair<>(createItem(Items.lava_bucket, 0, "Lava"), Citybuild.LAVA));
		CB_ITEMS.add(new Pair<>(createItem(beacon, 0, "Event"), Citybuild.EVENT));
	}

	public static ItemStack MISSING_TEXTURE = new ItemStack(Blocks.stone, 1, 10000);
	private final DropDownMenu<ItemStack> menu = new DropDownMenu<>(null, 0, 0, 0, 0);
	private final ModTextField textField = new ModTextField(-2, LabyModCore.getMinecraft().getFontRenderer(), 50, 0, 116, 20);
	private final List<ItemStack> allItems;
	private final ArrayList<ItemStack> items = Reflection.get(menu, "list");
	private final List<Consumer<ItemStack>> callbacks = new ArrayList<>();
	private final boolean sorted;
	private ItemStack currentItem = null;
	private ItemStack itemIcon;

	public CitybuildSettingImpl() {
		super("§cNo name set", new DropDownMenu<DummyEnum>(null, 0, 0, 0, 0) {
			@Override
			public void draw(int mouseX, int mouseY) {}
		});
		setChangeListener(v -> set(menu.getSelected()));

		List<ItemStack> items = CB_ITEMS.stream().map(p -> p.a).collect(Collectors.toList());
		iconData = new IconData();
		itemIcon = MISSING_TEXTURE;
		textField.setEnableBackgroundDrawing(false);
		textField.setMaxStringLength(Integer.MAX_VALUE);

		this.sorted = false;

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

	public Citybuild get() {
		for (Pair<ItemStack, Citybuild> pair : CB_ITEMS) {
			if (pair.a == menu.getSelected())
				return pair.b;
		}

		return ANY;
	}

	@Override
	public CitybuildSetting set(Citybuild value) {
		for (Pair<ItemStack, Citybuild> pair : CB_ITEMS) {
			if (pair.b == value)
				return set(pair.a);
		}

		return set(ANY);
	}

	public CitybuildSetting set(ItemStack stack) {
		Citybuild cb = ANY;
		for (Pair<ItemStack, Citybuild> pair : CB_ITEMS) {
			if (pair.a == menu.getSelected()) {
				cb = pair.b;
				break;
			}
		}

		Laby3Setting.super.set(cb);
		currentItem = stack;
		menu.setSelected(stack);
		filterItems();

		callbacks.forEach(c -> c.accept(stack));
		itemIcon = stack;
		return this;
	}

	public boolean isOpen() {
		return menu.isOpen();
	}

	public CitybuildSetting defaultValue(ItemStack defaultValue) {
		if (currentItem == null) {
			set(defaultValue);
		}
		return this;
	}

	public CitybuildSetting callback(Consumer<ItemStack> callback) {
		callbacks.add(callback);
		return this;
	}

	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		mouseOver = menu.isMouseOver(mouseX, mouseY);
		LabyMod.getInstance().getDrawUtils().drawItem(itemIcon == null ? MISSING_TEXTURE : itemIcon, x + 5, y + 3, null);

		super.draw(x, y, maxX, maxY, mouseX, mouseY);

		LabyMod.getInstance().getDrawUtils().drawRectangle(x - 1, y, x, maxY, ModColor.toRGB(120, 120, 120, 120));
		int width = Math.min(125, 165 - LabyMod.getInstance().getDrawUtils().getStringWidth(getDisplayName()));
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
			if (menu.getSelected() != currentItem)
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

		Reflection.set(menu, "scrollbar", scrollbar);
	}

	public void onScrollDropDown() {
		menu.onScroll();
	}

	@Override
	public DropDownMenu<ItemStack> getDropDownMenu() {
		return menu;
	}

	public enum DummyEnum {}

}