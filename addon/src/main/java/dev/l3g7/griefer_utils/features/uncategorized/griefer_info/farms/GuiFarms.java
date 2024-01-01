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

package dev.l3g7.griefer_utils.features.uncategorized.griefer_info.farms;

import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.BigChestUtil;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.gui.GuiBigChest;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.gui.GuiGrieferInfo;
import dev.l3g7.griefer_utils.features.uncategorized.griefer_info.gui.GuiSearchable;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

public class GuiFarms extends GuiSearchable<Farm> {

	private final GuiFilterType FILTER_SELECT_GUI = new GuiFilterType(this);
	private final GuiBigChest CB_SELECT_GUI = new GuiBigChest("§8§lFarmen - Citybuild", 7, this);

	static SpawnerType typeFiler = null;
	private String cbFilter = null;
	private boolean filteringForActive = true;
	private boolean filteringForPassive = true;
	private boolean hasClicked = false;
	private List<Farm> allCurrentFarms = new ArrayList<>();

	public GuiFarms() {
		super("§8§lFarmen", 7, GuiGrieferInfo.GUI, "Farm suchen...", Comparator.comparing(farm -> {
			int spawnerCount = typeFiler == null ? farm.spawnerCount : farm.spawnerTypes.get(typeFiler);
			return ((char) Short.MAX_VALUE - spawnerCount) + farm.name + farm.id;
		}));

		typeFiler = null;

		for (Map.Entry<Integer, ItemStack> cb : BigChestUtil.getCBs().entrySet()) {
			String asFilter = cb.getValue().getDisplayName().replaceAll("§.", "");

			CB_SELECT_GUI.addItem(cb.getKey(), cb.getValue(), () -> {
				cbFilter = asFilter;
				this.open();
			});
		}
	}

	@Override
	public void open() {
		super.open();
		addFilters(this);

		ItemStack removeItemFilter = ItemUtil.createItem(new ItemStack(Blocks.barrier), typeFiler != null, "§fKein Typ auswählen");
		if (typeFiler != null)
			ItemUtil.setLore(removeItemFilter, "§7Derzeitiger Typ: §6§n" + typeFiler.germanName);

		addFilters(FILTER_SELECT_GUI);
		FILTER_SELECT_GUI.addItem(19, removeItemFilter, () -> {
			typeFiler = null;
			open();
		});

		// Init cb selection
		addFilters(CB_SELECT_GUI);
		ItemStack removeCBFilter = ItemUtil.createItem(new ItemStack(Blocks.barrier), cbFilter != null, "§fKeinen Citybuild auswählen");
		if (cbFilter != null)
			ItemUtil.setLore(removeCBFilter, "§7Derzeitiger Citybuild: §6§n" + cbFilter);

		CB_SELECT_GUI.addItem(10, removeCBFilter, () -> {
			cbFilter = null;
			open();
		});
	}

	public void addFilters(GuiBigChest chest) {
		ItemStack cbStack = ItemUtil.createItem(Items.dark_oak_door, 0, "§fNach Citybuild filtern");
		if (cbFilter != null)
			for (ItemStack value : BigChestUtil.getCBs().values())
				if (cbFilter.equals(value.getDisplayName().replaceAll("§.", "")))
					cbStack = ItemUtil.createItem(value.copy(), true, "§fNach Citybuild filtern");

		if (cbFilter != null)
			ItemUtil.setLore(cbStack, "§7Derzeitiger Citybuild: §6§n" + cbFilter);

		chest.addItem(10, cbStack, CB_SELECT_GUI::open);

		ItemStack typeStack = ItemUtil.createItem(Items.spawn_egg, 50, "§fNach Typ filtern");

		if (typeFiler != null) {
			typeStack = ItemUtil.createItem(new ItemStack(typeFiler.isCobblestone() ? Blocks.cobblestone : Blocks.command_block), true, "§fNach Typ filtern");
			ItemUtil.setLore(typeStack, "§7Derzeitiger Typ: §6§n" + typeFiler.germanName);
		}

		if (typeFiler == null || typeFiler.isCobblestone())
			chest.addItem(19, typeStack, FILTER_SELECT_GUI::open);
		else
			chest.addTextureItem(19, new TextureItem(typeFiler.texture, 12, typeStack), FILTER_SELECT_GUI::open);

		TextureItem textureItem = new TextureItem("griefer_info/" + (filteringForActive ? "diamond" : "gray") + "_sword", (filteringForActive ? "§f" : "§7") + "Aktiv", "§8Klicke, um Umzuschalten");
		chest.addTextureItem(28, textureItem, () -> {
			filteringForActive = !filteringForActive;
			if (!hasClicked) {
				filteringForActive = hasClicked = true;
				filteringForPassive = false;
			}

			if (!filteringForActive)
				filteringForPassive = true;

			open();
		});

		textureItem = new TextureItem("griefer_info/chest" + (filteringForPassive ? "" : "_gray"), (filteringForPassive ? "§f" : "§7") + "Passiv", "§8Klicke, um Umzuschalten");
		chest.addTextureItem(37, textureItem, () -> {
			filteringForPassive = !filteringForPassive;
			if (!hasClicked) {
				filteringForPassive = hasClicked = true;
				filteringForActive = false;
			}

			if (!filteringForPassive)
				filteringForActive = true;

			open();
		});
	}

	@Override
	protected void renderToolTip(ItemStack stack, int x, int y) {
		super.renderToolTip(stack, x, y);
		if (stack.getItem() != Item.getItemFromBlock(Blocks.grass) && stack.getItem() != Item.getItemFromBlock(Blocks.dirt))
			return;

		List<String> tooltip = stack.getTooltip(player(), false);
		if (tooltip.isEmpty())
			return;

		List<String> textures = new ArrayList<>();
		NBTTagList texturesNbt = stack.getTagCompound().getTagList("textures_nbt", 8);
		for (int i = 0; i < texturesNbt.tagCount(); i++)
			textures.add(texturesNbt.getStringTagAt(i));

		// Render
		Pair<Integer, Integer> tooltipTranslation = RenderUtil.getTooltipTranslation(tooltip, x, y, width, height);
		int tooltipY = tooltipTranslation.getRight() + 12;

		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, 500);
		for (String texture : textures) {
			if (texture.endsWith("stonebrick")) {
				GlStateManager.scale(0.5, 0.5, 1);
				drawUtils().renderItemIntoGUI(new ItemStack(Blocks.cobblestone), tooltipTranslation.getLeft() * 2 - 2.5, tooltipY * 2 - 1);
				GlStateManager.scale(2, 2, 1);
			} else {
				GlStateManager.disableLighting();
				drawUtils().bindTexture("griefer_utils/icons/" + texture + ".png");
				drawUtils().drawTexture(tooltipTranslation.getLeft() - 0.5, tooltipY, 256, 256, 7, 7);
			}
			tooltipY += 10;
		}
		GlStateManager.popMatrix();
	}

	@Override
	protected int fillEntriesOnEmptySearch(TreeSet<Farm> entries) {
		for (Farm farm : new ArrayList<>(Farm.FARMS))
			if (farm.matchesFilter(cbFilter, typeFiler, filteringForActive, filteringForPassive))
				entries.add(farm);

		allCurrentFarms = new ArrayList<>(entries);
		updatePage();
		return entries.size();
	}

	@Override
	protected Iterable<Farm> getAllEntries() {
		return allCurrentFarms;
	}

	@Override
	protected void addItem(Farm entry, int slot) {
		entry.addItemStack(this, slot, typeFiler, cbFilter != null, scrollStartRow % 2 == slot / 9 % 2);
	}

	@Override
	protected boolean matchesSearch(Farm entry, String search) {
		return entry.name.toLowerCase().contains(search);
	}

}
