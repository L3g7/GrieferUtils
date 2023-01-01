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

package dev.l3g7.griefer_utils.features.chat.chat_reactor;

import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;

public class ReactionDisplaySetting extends CategorySetting {

	private final SettingsElement parent;
	private boolean editHovered = false;
	public final ChatReaction reaction;

	public ReactionDisplaySetting(ChatReaction reaction, SettingsElement parent) {
		name("§f");
		this.parent = parent;
		this.reaction = reaction;
		int size = parent.getSubSettings().getElements().size();
		int pos = reaction.pos = reaction.pos == -1 ? size - 1 : reaction.pos;
		parent.getSubSettings().getElements().add(pos, this);
	}

	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (editHovered) {
			parent.getSubSettings().getElements().remove(this);
			ChatReactor.saveEntries();
			Minecraft.getMinecraft().displayGuiScreen(new AddChatReactionGui(reaction, Minecraft.getMinecraft().currentScreen));
		}
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		String displayName = getDisplayName();
		setDisplayName("§f");
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		setDisplayName(displayName);

		String trimmedTrigger = drawUtils().trimStringToWidth(reaction.trigger, maxX - x - 25 - 25);
		String trimmedCommand = drawUtils().trimStringToWidth(reaction.command, maxX - x - 25 - 25);
		drawUtils().drawString(trimmedTrigger + (trimmedTrigger.equals(reaction.trigger) ? "" : "…"), x + 25, y + 7 - 5);
		drawUtils().drawString("§o➡ " + trimmedCommand + (trimmedCommand.equals(reaction.command) ? "" : "…"), x + 25, y + 7 + 5);

		if (mouseOver) {
			mc.getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/pencil.png"));
			drawUtils().drawTexture( maxX - 16 - 3, y + 4.5, 256, 256, 14, 14);
			editHovered = mouseX > maxX - 19 && mouseX < maxX - 6 && mouseY > y + 5 && mouseY < y + 18;
		}
	}

}