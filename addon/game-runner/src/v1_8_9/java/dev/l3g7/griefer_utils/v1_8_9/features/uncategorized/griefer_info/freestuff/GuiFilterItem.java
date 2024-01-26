/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.griefer_info.freestuff;

import dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.griefer_info.freestuff.ItemFilter.Category;
import dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.griefer_info.gui.GuiSearchable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import static dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.griefer_info.BigChestUtil.toSlotId;

public class GuiFilterItem extends GuiSearchable<ItemFilter> {

	private final GuiFreestuff guiFreestuff;
	private Category currentCategory = null;
	private List<ItemFilter> allCurrentItems = new ArrayList<>();

	public GuiFilterItem(GuiFreestuff guiFreestuff) {
		super("§8§lFreestuff", 7, guiFreestuff, "Item suchen...", Comparator.comparing(iF -> iF.stack.getDisplayName()));
		this.guiFreestuff = guiFreestuff;
	}

	@Override
	public void open() {
		super.open();
		currentCategory = null;
		addItem((rows - 1) * 9, BACK_BUTTON, guiFreestuff::open);
	}

	@Override
	protected int fillEntriesOnEmptySearch(TreeSet<ItemFilter> itemFilters) {
		if (currentCategory != null) {
			itemFilters.addAll(currentCategory.itemFilters);
			allCurrentItems = new ArrayList<>(itemFilters);
			updatePage();
			return itemFilters.size();
		}

		for (Category category : ItemFilter.CATEGORIES) {
			itemFilters.addAll(category.itemFilters);
			addItem(toSlotId(category.ordinal(), false), category.stack, () -> {
				entries.clear();
				entries.addAll(category.itemFilters);
				entryCount = entries.size();
				allCurrentItems = new ArrayList<>(entries);
				currentCategory = category;
				addItem((rows - 1) * 9, BACK_BUTTON, () -> {
					currentCategory = null;
					entries.clear();
					entries.addAll(allCurrentItems);
					this.open();
				});
				updatePage();
			});

			allCurrentItems = new ArrayList<>(itemFilters);
		}

		return ItemFilter.CATEGORIES.size();
	}

	@Override
	protected Iterable<ItemFilter> getAllEntries() {
		return allCurrentItems;
	}

	@Override
	protected void addItem(ItemFilter entry, int slot) {
		addItem(slot, entry.stack, () -> {
			searchField.setText("");
			guiFreestuff.itemFilter = entry;
			guiFreestuff.open();
		});
	}

	@Override
	protected boolean matchesSearch(ItemFilter entry, String search) {
		return entry.matchesFilter(search);
	}

}
