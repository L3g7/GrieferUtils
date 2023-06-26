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

package dev.l3g7.griefer_utils.features.uncategorized.griefer_info;

import net.labymod.gui.elements.ModTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.*;

import static dev.l3g7.griefer_utils.features.uncategorized.griefer_info.BigChestUtil.toSlotId;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

public abstract class GuiSearchable<T> extends GuiBigChest {

	private static final ResourceLocation SEARCH_TAB_TEXTURE = new ResourceLocation("textures/gui/container/creative_inventory/tab_item_search.png");
	private static final ResourceLocation SCROLLBAR_TEXTURE = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");

	protected final ModTextField searchField;
	private final int searchFieldWidth;
	protected int entryCount = 0;
	protected int scrollStartRow = 0;
	private float currentScroll = 0;
	private boolean wasClicking = false;
	private boolean isScrolling = false;

	public TreeSet<T> entries;
	public GuiSearchable(String title, int rows, GuiBigChest previousGui, String searchPlaceHolder, int searchFieldWidth, Comparator<T> comparator) {
		super(title, rows, previousGui);

		searchField = new ModTextField(0, mc().fontRendererObj, guiLeft + 170 - searchFieldWidth, guiTop + 6, searchFieldWidth - 7, mc().fontRendererObj.FONT_HEIGHT);
		searchField.setPlaceHolder("§o" + searchPlaceHolder);
		searchField.setTextColor(0xFFFFFF);
		searchField.setMaxStringLength(Integer.MAX_VALUE);
		searchField.setEnableBackgroundDrawing(false);

		this.searchFieldWidth = searchFieldWidth;
		this.entries = new TreeSet<>(comparator);
	}

	public GuiSearchable(String title, int rows, GuiBigChest previousGui, String searchPlaceHolder, Comparator<T> comparator) {
		this(title, rows, previousGui, searchPlaceHolder, 88, comparator);
	}

	@Override
	public void open() {
		super.open();
		clearSearch();
	}

	public void clearSearch() {
		searchField.setText("");
		currentScroll = 0;
		entryCount = onSearch("");
	}

	protected void clearItems() {
		for (int i = 0; i < 25; i++)
			addItem(toSlotId(i, false), null, null);
	}

	protected abstract int fillEntriesOnEmptySearch(TreeSet<T> entries);

	protected abstract Iterable<T> getAllEntries();

	protected abstract void addItem(T entry, int slot);

	protected abstract boolean matchesSearch(T entry, String search);

	protected void updatePage() {
		clearItems();
		Iterator<T> iterator = entries.iterator();

		for (int i = 0; i < 25; i++) {
			if (!iterator.hasNext()) {
				addItem(toSlotId(i, false), null, null);
				continue;
			}

			addItem(iterator.next(), toSlotId(i, false));
		}
	}

	private int onSearch(String searchString) {
		entries.clear();
		clearItems();

		// Without filter
		if (searchString.isEmpty())
			return fillEntriesOnEmptySearch(entries);

		// With filter
		for (T entry : getAllEntries())
			if (matchesSearch(entry, searchString.toLowerCase()))
				entries.add(entry);

		updatePage();
		return this.entries.size();
	}

	private void onScroll(float scroll) {
		int i = (entries.size() + 5 - 1) / 5 - 5;
		scrollStartRow = Math.max((int) (scroll * i + 0.5), 0);

		List<T> entriesAsList = new ArrayList<>(entries);

		for (int y = 0; y < 5; ++y) {
			for (int x = 0; x < 5; ++x) {
				int index = x + (y + scrollStartRow) * 5;
				int slotId = toSlotId(x + y * 5, false);

				if (index >= 0 && index < entries.size())
					addItem(entriesAsList.get(index), slotId);
				else
					addItem(slotId, null, null);
			}
		}
	}


	@Override
	public void initGui() {
		super.initGui();
		searchField.xPosition = guiLeft + 170 - searchFieldWidth;
		searchField.yPosition = guiTop + 6;
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (searchField.textboxKeyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey())) {
			clearItems();
			entryCount = onSearch(searchField.getText());
			return;
		}

		super.keyTyped(typedChar, keyCode);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton != -1)
			searchField.mouseClicked(mouseX, mouseY, Mouse.getEventButton());

		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		boolean isLeftClicking = Mouse.isButtonDown(0);
		int left = guiLeft + 175;
		int top = guiTop + 18;
		int right = left + 14;
		int bottom = top + 112;

		if (!wasClicking && isLeftClicking && mouseX >= left && mouseY >= top && mouseX < right && mouseY < bottom)
			isScrolling = entryCount > 25;

		if (!isLeftClicking)
			isScrolling = false;

		wasClicking = isLeftClicking;

		if (isScrolling) {
			currentScroll = ((mouseY - top) - 7.5f) / ((bottom - top) - 15f);
			currentScroll = MathHelper.clamp_float(currentScroll, 0, 1);
			onScroll(currentScroll);
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);

		GlStateManager.color(1, 1, 1, 1);

		GlStateManager.translate(-guiLeft, -guiTop, 0);
		searchField.drawTextBox();
		GlStateManager.translate(guiLeft, guiTop, 0);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		GlStateManager.enableAlpha();
		mc().getTextureManager().bindTexture(SEARCH_TAB_TEXTURE);
		drawTexturedModalRect(guiLeft + 167 - searchFieldWidth, guiTop, 80, 0, searchFieldWidth + 1, 17);
		drawTexturedModalRect(guiLeft + 168, guiTop, 169, 0, 26, 17);
		drawTexturedModalRect(guiLeft + 169, guiTop + 17, 170, 17, 25, 18);
		drawTexturedModalRect(guiLeft + 169, guiTop + ySize - 25, 170, 111, 25, 25);

		for (int i = 0; i < rows - 2; i++)
			drawTexturedModalRect(guiLeft + 169, guiTop + 35 + i * 18, 170, 35, 25, 18);

		int sbStart = guiTop + 18;
		int sbEnd = guiTop + ySize - 6;
		mc.getTextureManager().bindTexture(SCROLLBAR_TEXTURE);
		drawTexturedModalRect(guiLeft + 174, sbStart + ((sbEnd - sbStart - 17) * currentScroll), entryCount > 25 ? 232 : 244, 0, 12, 15);
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		int dWheel = Mouse.getEventDWheel();

		if (dWheel == 0 || entryCount <= 25)
			return;

		float invisibleRows = entryCount / 5f - 5;

		if (dWheel > 0)
			dWheel = 1;

		if (dWheel < 0)
			dWheel = -1;

		currentScroll = currentScroll - dWheel / invisibleRows;
		currentScroll = MathHelper.clamp_float(currentScroll, 0.0F, 1.0F);
		onScroll(currentScroll);
	}

	public void onEntryData() {
		onSearch(searchField.getText());
	}

}
