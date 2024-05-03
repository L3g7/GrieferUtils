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

package dev.l3g7.griefer_utils.v1_8_9.features.chat.chat_filter_templates.laby3;

import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.v1_8_9.features.chat.chat_filter_templates.laby3.ChatFilterTemplates.FilterTemplate;
import net.labymod.core.LabyModCore;
import net.labymod.ingamechat.tabs.GuiChatFilter;
import net.labymod.ingamechat.tools.filter.Filters;
import net.labymod.utils.ModColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.main.GameConfiguration;

import java.io.IOException;

public class GuiChatFilterWithTemplates extends GuiChatFilter {

	public boolean templatesOpen = false;

	public GuiChatFilterWithTemplates(String defaultText) {
		super(defaultText);
		System.out.println(">:> GuiChatFilterWithTemplates");
	}

	// TODO: Inject into open gui instead of overwriting
	// TODO: replace numbers with constants

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		System.out.println(">:> drawScreen");
		super.drawScreen(mouseX, mouseY, partialTicks);
		Filters.Filter selectedFilter = Reflection.get(this, "selectedFilter");
		if (selectedFilter != null)
			return;

		renderTemplates(mouseX, mouseY);
	}

	@Override
	public void setWorldAndResolution(Minecraft lvt_1_1_, int lvt_2_1_, int lvt_3_1_) {
		System.out.println(">:> setWorldAndResolution");
		super.setWorldAndResolution(lvt_1_1_, lvt_2_1_, lvt_3_1_);
	}

	public void renderTemplates(int mouseX, int mouseY) {
		if (!templatesOpen) {
			boolean hover = mouseX > this.width - 165 && mouseX < this.width - 152 && mouseY > this.height - 204 && mouseY < this.height - 204 + 13;
			drawRect(this.width - 165, this.height - 204, this.width - 152, this.height - 204 + 13, hover ? Integer.MAX_VALUE : Integer.MIN_VALUE);
			this.drawCenteredString(LabyModCore.getMinecraft().getFontRenderer(), "v", this.width - 158, this.height - 202, hover ? ModColor.toRGB(50, 220, 120, 210) : Integer.MAX_VALUE);
		} else {
			drawRect(this.width - 270, this.height - 220, this.width - 152, this.height - 16, Integer.MIN_VALUE);
			int relX = this.width - 270;
			int relY = this.height - 220;
			drawCenteredString(LabyModCore.getMinecraft().getFontRenderer(), "§nVorlagen", relX + 53, relY + 2, Integer.MAX_VALUE);

			boolean hoverTemplate = mouseX > this.width - 270 && mouseX < this.width - 152 && mouseY > this.height - 220 + 12 && mouseY < this.height - 16;
			int hoveredIndex = -1;
			if (hoverTemplate)
				hoveredIndex = (mouseY - this.height + 220 - 13) / 11;

			relY += 13;
			int currentIndex = 0;
			for (FilterTemplate template : ChatFilterTemplates.TEMPLATES) {
				if (currentIndex == hoveredIndex)
					drawRect(relX, relY, relX + 118, relY + 11, 0x70000000);

				drawString(LabyModCore.getMinecraft().getFontRenderer(), template.name, relX + 2, relY + 2, Integer.MAX_VALUE);
				relY += 11;
				currentIndex++;
			}

			boolean hoverCancel = mouseX > this.width - 268 && mouseX < this.width - 154 && mouseY > this.height - 30 && mouseY < this.height - 18;
			drawRect(this.width - 268, this.height - 30, this.width - 154, this.height - 18, hoverCancel ? ModColor.toRGB(200, 100, 100, 200) : Integer.MAX_VALUE);
			this.drawCenteredString(LabyModCore.getMinecraft().getFontRenderer(), "Abbrechen", this.width - 262 + 50, this.height - 30 + 2, Integer.MAX_VALUE);
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		Filters.Filter selectedFilter = Reflection.get(this, "selectedFilter");
		if (selectedFilter != null || handleTemplateClick(mouseX, mouseY)) {
			try {
				super.mouseClicked(mouseX, mouseY, mouseButton);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public boolean handleTemplateClick(int mouseX, int mouseY) {
		if (mouseX > this.width - 165 && mouseX < this.width - 152 && mouseY > this.height - 204 && mouseY < this.height - 204 + 13) {
			templatesOpen = true;
			return false;
		}

		boolean hoverCancel = mouseX > this.width - 268 && mouseX < this.width - 154 && mouseY > this.height - 30 && mouseY < this.height - 18;
		if (templatesOpen && hoverCancel) {
			templatesOpen = false;
			return false;
		}

		boolean hoverTemplate = mouseX > this.width - 270 && mouseX < this.width - 152 && mouseY > this.height - 220 + 12 && mouseY < this.height - 16;
		if (templatesOpen && hoverTemplate) {
			int hoveredIndex = (mouseY - this.height + 220 - 13) / 11;
			if (hoveredIndex < ChatFilterTemplates.TEMPLATES.length) {
				loadTemplate(ChatFilterTemplates.TEMPLATES[hoveredIndex]);
				templatesOpen = false;
			}
		}

		return true;
	}

	protected void actionPerformed(GuiButton button) {
		// TODO: required for Intellij to stfu, but why?
		try {
			super.actionPerformed(button);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void loadTemplate(FilterTemplate template) {
		Reflection.invoke(this, "loadFilter", template.toFilter());
	}

}