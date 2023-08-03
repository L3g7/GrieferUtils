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

package dev.l3g7.griefer_utils.features.item.item_saver.tool_saver;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import net.labymod.settings.elements.ControlElement;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;

class ItemDisplaySetting extends ControlElement implements ElementBuilder<ItemDisplaySetting> {

	final String name;
	final ItemStack stack;

	private final IconStorage iconStorage = new IconStorage();
	private boolean hoveringDelete = false;

	ItemDisplaySetting(String name, ItemStack stack) {
		super(name, null);
		this.name = name;
		setSettingEnabled(false);
		this.stack = stack;
		icon(stack);
	}

	@Override
	public int getObjectWidth() {
		return 0;
	}

	@Override
	public IconStorage getIconStorage() {
		return iconStorage;
	}

	public ItemStack getStack() {
		return stack;
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (!hoveringDelete)
			return;

		mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
		ToolSaver ts = FileProvider.getSingleton(ToolSaver.class);
		ts.enabled.getSubSettings().getElements().remove(this);
		ts.onChange();
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		drawIcon(x, y);

		int xPosition = maxX - 20;
		double yPosition = y + 4.5;

		hoveringDelete = mouseX >= xPosition && mouseY >= yPosition && mouseX <= xPosition + 15.5 && mouseY <= yPosition + 16;

		if (!mouseOver)
			return;

		mc.getTextureManager().bindTexture(new ResourceLocation("labymod/textures/misc/blocked.png"));
		drawUtils().drawTexture(maxX - (hoveringDelete ? 20 : 19), y + (hoveringDelete ? 3.5 : 4.5), 256, 256, hoveringDelete ? 16 : 14, hoveringDelete ? 16 : 14);
	}

}
