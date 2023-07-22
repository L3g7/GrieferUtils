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

import dev.l3g7.griefer_utils.misc.gui.guis.AddonsGuiWithCustomBackButton;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.StringSetting;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.Material;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;

public class ItemDisplaySetting extends ControlElement implements ElementBuilder<ItemDisplaySetting> {

	public final StringSetting name;
	public final BooleanSetting drop;
	public final BooleanSetting extremeDrop;
	public final BooleanSetting leftclick;
	public final BooleanSetting rightclick;

	private final IconStorage iconStorage = new IconStorage();
	private final ItemStack stack;
	private boolean hoveringDelete = false;
	private boolean hoveringEdit = false;

	public ItemDisplaySetting(ItemStack stack) {
		super("§cNo name set", null);
		setSettingEnabled(false);
		this.stack = stack;
		icon(stack);
		name(stack.getDisplayName());

		name = new StringSetting()
			.name("Anzeigename")
			.description("Der Anzeigename des Eintrags. Hat keinen Einfluss auf die geretten Items.")
			.defaultValue(stack.getDisplayName())
			.callback(this::name)
			.icon(Material.BOOK_AND_QUILL);

		drop = new BooleanSetting()
			.name("Droppen unterbinden")
			.description("Ob das Droppen dieses Items unterbunden werden soll.")
			.defaultValue(true)
			.icon(Material.DROPPER);

		extremeDrop = new BooleanSetting()
			.name("Droppen unterbinden (extrem)")
			.description("Ob das Aufnehmen dieses Items in den Maus-Cursor unterbunden werden soll.")
			.icon("shield_with_sword")
			.defaultValue(false)
			.callback(b -> { if (b) drop.set(true); });

		drop.callback(b -> { if (!b) extremeDrop.set(false); });

		leftclick = new BooleanSetting()
			.name("Linksklicks unterbinden")
			.description("Ob Linksklicks mit diesem Item unterbunden werden soll.")
			.defaultValue(stack.isItemStackDamageable())
			.icon(Material.DIAMOND_SWORD);

		rightclick = new BooleanSetting()
			.name("Rechtsklicks unterbinden")
			.description("Ob Rechtsklicks mit diesem Item unterbunden werden soll.")
			.defaultValue(!stack.isItemStackDamageable())
			.icon(Material.BOW);

		subSettings(name, drop, extremeDrop, leftclick, rightclick);
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

		if (hoveringEdit) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
			mc.displayGuiScreen(new AddonsGuiWithCustomBackButton(ItemSaver::onChange, this));
			return;
		}

		if (!hoveringDelete)
			return;

		mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
		ItemSaver.enabled.getSubSettings().getElements().remove(this);
		ItemSaver.onChange();
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		hideSubListButton();
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		drawIcon(x, y);

		mouseOver = mouseX > x && mouseX < maxX && mouseY > y && mouseY < maxY;

		int xPosition = maxX - 20;
		double yPosition = y + 4.5;

		hoveringDelete = mouseX >= xPosition && mouseY >= yPosition && mouseX <= xPosition + 15.5 && mouseY <= yPosition + 16;

		xPosition -= 20;

		hoveringEdit = mouseX >= xPosition && mouseY >= yPosition && mouseX <= xPosition + 15.5 && mouseY <= yPosition + 16;

		if (!mouseOver)
			return;

		mc.getTextureManager().bindTexture(new ResourceLocation("labymod/textures/misc/blocked.png"));
		drawUtils().drawTexture(maxX - (hoveringDelete ? 20 : 19), y + (hoveringDelete ? 3.5 : 4.5), 256, 256, hoveringDelete ? 16 : 14, hoveringDelete ? 16 : 14);

		mc.getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/pencil.png"));
		drawUtils().drawTexture(maxX - (hoveringEdit ? 40 : 39), y + (hoveringEdit ? 3.5 : 4.5), 256, 256, hoveringEdit ? 16 : 14, hoveringEdit ? 16 : 14);
	}

}
