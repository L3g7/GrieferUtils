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

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.misc.gui.elements.Button;
import dev.l3g7.griefer_utils.misc.gui.elements.Gui;
import dev.l3g7.griefer_utils.misc.gui.elements.SelectButtonGroup;
import dev.l3g7.griefer_utils.misc.gui.elements.TextField;
import dev.l3g7.griefer_utils.util.AddonUtil;
import net.labymod.settings.LabyModModuleEditorGui;
import net.labymod.settings.PreviewRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;

// TODO: support for tab, scrolling, citybuild, matchAll, editing, disable saveButton if incomplete
public class AddChatReactionGui extends Gui {

	private static final int RENDER_GROUP_POST = 1; // Drawable objects rendered after everything else
	private static final int RENDER_GROUP_SELECTED = 2; // Rendered if a text type is selected

	private final GuiScreen backgroundScreen;

	private SelectButtonGroup<TextType> textTypeInput;
	private TextField triggerInput;
	private TextField commandInput;

	private Button cancelButton;
	private Button saveButton;

	@SuppressWarnings("unused")
	public AddChatReactionGui(ChatReaction reaction, GuiScreen backgroundScreen) {
		this.backgroundScreen = backgroundScreen;
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void initGui() {
		super.initGui();
		backgroundScreen.width = width;
		backgroundScreen.height = height;
		if (backgroundScreen instanceof LabyModModuleEditorGui)
			PreviewRenderer.getInstance().init(AddChatReactionGui.class);

		int center = width / 2;
		int top = 50 + 80; // 50px header + 80px breadcrumb
		int padding = 35; // 35px between inputs

		// Header and breadcrumb
		createCenteredText("§e§l" + Constants.ADDON_NAME, 1.3).pos(center, 81);
		createCenteredText("§e§lChatReactor", .7).pos(center, 105);
		createButton("<") // Back button
			.pos(center - 100, 20)
			.size(22, 20)
			.renderGroup(RENDER_GROUP_POST)
			.callback(this::close);

		// cancel and save button
		int buttonWidth = 100;
		cancelButton = createButton("Abbrechen")
			.x(center - buttonWidth - 5) // 2 * 5px padding between buttons
			.size(buttonWidth, 20)
			.callback(this::close);
		saveButton = createButton("Hinzufügen")
			.x(center + 5) // 2 * 5px padding between buttons
			.size(buttonWidth, 20)
			.callback(this::save);

		// Text type selection
		textTypeInput = createSelectGroup(TextType.NONE, "Text-Form")
			.y(top);

		int width = textTypeInput.width();
		int x = center - width / 2;

		// Trigger input
		triggerInput = createTextField("<Von Auswahl abhängiges Label>")
			.pos(x, textTypeInput.bottom() + padding)
			.width(width)
			.renderGroup(RENDER_GROUP_SELECTED);

		// Command input
		commandInput = createTextField("Befehl")
			.pos(x, triggerInput.bottom() + padding)
			.width(width)
			.renderGroup(RENDER_GROUP_SELECTED);
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// Draw background
		drawUtils.drawAutoDimmedBackground(0);

		if (textTypeInput.getSelected() == TextType.NONE) {
			cancelButton.yPosition = saveButton.yPosition = (int) textTypeInput.bottom() + 35; // 35px padding (as in initGui)
		} else {
			if (textTypeInput.getSelected() == TextType.TEXT) {
				triggerInput.label("Text").placeholder("[GrieferUtils] /freekiste ist nun verfügbar!");
				commandInput.placeholder("/gu:run_on_cb /freekiste");
			} else {
				triggerInput.label("Regulärer Ausdruck").placeholder("§8^\\[[^ ]+ ┃ ([^ ]+) -> mir] (.*)$");
				commandInput.placeholder("§8/msg MainAcc \\1: \\2");
			}

			cancelButton.yPosition = saveButton.yPosition = (int) commandInput.bottom() + 35; // 35px padding (as in initGui)
			draw(mouseX, mouseY, RENDER_GROUP_SELECTED);
		}


		// Draw content
		draw(mouseX, mouseY);

		// Draw header
		int headerSize = 45;
		drawUtils.drawOverlayBackground(0, headerSize); // header background
		drawUtils.drawGradientShadowTop(headerSize, 0.0, this.width); // header gradient

		// Draw currently open addon
		int actionSize = 100;
		drawUtils.drawString("GrieferUtils", width / 2d - actionSize + 30, 25);
		int addonIconSize = 20;
		drawUtils.drawImageUrl(AddonUtil.getInfo().getImageURL(), width / 2d + actionSize - addonIconSize, 20, 256, 256, addonIconSize, addonIconSize);

		// Draw footer
		int footerSize = 10;
		drawUtils.drawOverlayBackground(height - footerSize, height); // footer background
		drawUtils.drawGradientShadowBottom(height - footerSize, 0, width); // footer gradient

		// Draw post
		draw(mouseX, mouseY, RENDER_GROUP_POST);
	}

	private void close() {
		backgroundScreen.initGui();
		Minecraft.getMinecraft().displayGuiScreen(backgroundScreen);
	}

	private void save() {
		ChatReaction reaction = new ChatReaction();
		reaction.regEx = textTypeInput.getSelected() == TextType.REGEX;
		reaction.trigger = triggerInput.getText();
		reaction.command = commandInput.getText();
		reaction.completed = true;

		new ReactionDisplaySetting(reaction, FileProvider.getSingleton(ChatReactor.class).getMainElement())
			.icon(textTypeInput.getSelected().getIcon());

		ChatReactor.saveEntries();
		close();
	}

	private enum TextType implements SelectButtonGroup.Selectable {

		NONE("", ""), TEXT("normaler Text", "yellow_t"), REGEX("regulärer Ausdruck", "regex");

		private final String name, icon;

		TextType(String name, String icon) {
			this.name = name;
			this.icon = icon;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getIcon() {
			return icon;
		}
	}
}