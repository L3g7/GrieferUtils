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

package dev.l3g7.griefer_utils.features.chat.chat_reactor;

import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;

public class ReactionDisplaySetting extends BooleanSetting {

	private final SettingsElement parent;
	private boolean editHovered = false;
	public final ChatReaction reaction;

	public ReactionDisplaySetting(ChatReaction reaction, SettingsElement parent) {
		name("§f");
		this.parent = parent;
		this.reaction = reaction;

		List<SettingsElement> reactions = parent.getSubSettings().getElements();
		reactions.add(reactions.size() - 1, this);
		set(reaction.enabled);
		callback(enabled -> reaction.enabled = enabled);
	}

	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (editHovered)
			Minecraft.getMinecraft().displayGuiScreen(new AddChatReactionGui(this, Minecraft.getMinecraft().currentScreen));
	}

	public void delete() {
		parent.getSubSettings().getElements().remove(this);
		ChatReactor.saveEntries();
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		icon(reaction.regEx ? "regex" : "yellow_t");

		String displayName = getDisplayName();
		setDisplayName("§f");
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		setDisplayName(displayName);

		String cb = "§e[" + MinecraftUtil.getCitybuildAbbreviation(reaction.citybuild.getDisplayName()) + "] ";
		int cbWidth = drawUtils().getStringWidth(cb);

		String trimmedTrigger = drawUtils().trimStringToWidth(reaction.trigger, maxX - x - 25 - 79);
		String trimmedCommand = drawUtils().trimStringToWidth(reaction.command, maxX - x - 25 - 79 - drawUtils().getStringWidth("➡ ") - cbWidth);
		drawUtils().drawString(trimmedTrigger + (trimmedTrigger.equals(reaction.trigger) ? "" : "…"), x + 25, y + 7 - 5);
		drawUtils().drawString("§o➡ " + trimmedCommand + (trimmedCommand.equals(reaction.command) ? "" : "…"), x + 25 + cbWidth, y + 7 + 5);
		drawUtils().drawString(cb, x + 25, y + 7 + 5);

		editHovered = mouseX > maxX - 70 && mouseX < maxX - 55 && mouseY > y + 4 && mouseY < y + 20;
		mc.getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/pencil.png"));
		drawUtils().drawTexture(maxX - 66 - (editHovered ? 4 : 3), y + (editHovered ? 3.5 : 4.5), 256, 256, editHovered ? 16 : 14, editHovered ? 16 : 14);
	}

	@Override
	public int getObjectWidth() {
		return super.getObjectWidth();
	}
}