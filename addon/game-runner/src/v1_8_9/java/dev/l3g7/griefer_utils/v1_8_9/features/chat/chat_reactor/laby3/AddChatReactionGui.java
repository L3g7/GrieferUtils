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

package dev.l3g7.griefer_utils.v1_8_9.features.chat.chat_reactor.laby3;

import dev.l3g7.griefer_utils.api.event.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.api.misc.Citybuild;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.api.misc.Named;
import dev.l3g7.griefer_utils.v1_8_9.misc.gui.elements.*;
import dev.l3g7.griefer_utils.v1_8_9.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil;
import net.labymod.gui.elements.Scrollbar;
import net.labymod.settings.LabyModModuleEditorGui;
import net.labymod.settings.PreviewRenderer;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.getButtonHeight;

public class AddChatReactionGui extends Gui {

	private static final int RENDER_GROUP_POST = 1; // Drawable objects rendered after everything else
	private static final int RENDER_GROUP_SELECTED = 2; // Rendered if a type is selected
	private static final int RENDER_GROUP_TEXT = 3; // Rendered if the text type is selected
	private static final int RENDER_GROUP_CITYBUILD_MENU = 4;

	private static final int PADDING = 35; // 35px between inputs
	private static final int HEADER_HEIGHT = 50;

	private final GuiScreen backgroundScreen;
	private final ReactionDisplaySetting editedReaction;

	// Input fields
	private SelectButtonGroup<TextType> textTypeInput;
	private TextField triggerInput;
	private TextField commandInput;
	private DropDown<TextCompareMode> compareModeInput;
	private DropDown<Citybuild> citybuildInput;

	private Scrollbar scrollbar;
	private Button backButton;
	private Button cancelButton;
	private Button saveButton;

	public AddChatReactionGui(ReactionDisplaySetting reaction, GuiScreen backgroundScreen) {
		this.backgroundScreen = backgroundScreen;
		editedReaction = reaction;
		EventRegisterer.register(this);
	}

	public void initGui() {
		super.initGui();
		backgroundScreen.width = width;
		backgroundScreen.height = height;
		if (backgroundScreen instanceof LabyModModuleEditorGui)
			PreviewRenderer.getInstance().init(AddChatReactionGui.class);

		int center = width / 2;
		int top = HEADER_HEIGHT + 80; // 80px for breadcrumb

		// Header and breadcrumb
		createCenteredText("§e§l" + Constants.ADDON_NAME, 1.3).pos(center, 81);
		createCenteredText("§e§lChatReactor", .7).pos(center, 105);
		backButton = createButton("<") // Back button
			.pos(center - 100, 20)
			.size(22, 20)
			.renderGroup(RENDER_GROUP_POST)
			.callback(this::close);

		// cancel and save button
		int buttonWidth = 100;
		cancelButton = createButton(editedReaction == null ? "Abbrechen" : "Löschen")
			.x(center - buttonWidth - 5) // 2 * 5px padding between buttons
			.size(buttonWidth, 20)
			.callback(() -> {
				if (editedReaction != null)
					editedReaction.delete();
				close();
			});

		saveButton = createButton(editedReaction == null ? "Hinzufügen" : "Bearbeiten")
			.x(center + 5) // 2 * 5px padding between buttons
			.size(buttonWidth, 20)
			.callback(this::save);

		// scrollbar
		scrollbar = new Scrollbar(1);
		scrollbar.setPosition(this.width / 2 + 172, 50, this.width / 2 + 172 + 4, this.height - 15);
		scrollbar.setSpeed(20);
		scrollbar.init();

		// Text type selection
		textTypeInput = createSelectGroup(TextType.NONE, "Text-Form")
			.y(top);

		int width = textTypeInput.width();
		int x = center - width / 2;

		// Trigger input
		triggerInput = createTextField("<Von Auswahl abhängiges Label>")
			.pos(x, textTypeInput.bottom() + PADDING)
			.width(width)
			.renderGroup(RENDER_GROUP_SELECTED);

		// Command input
		commandInput = createTextField("Befehl")
			.pos(x, triggerInput.bottom() + PADDING)
			.width(width)
			.renderGroup(RENDER_GROUP_SELECTED);

		compareModeInput = createDropDown(TextCompareMode.CONTAINS, "Auslösen")
			.y(commandInput.bottom() + PADDING)
			.width(width)
			.renderGroup(RENDER_GROUP_TEXT);

		citybuildInput = createDropDown(Citybuild.ANY, "Citybuild")
			.y(compareModeInput.bottom() + PADDING)
			.width(width)
			.renderGroup(RENDER_GROUP_SELECTED)
			.menuRenderGroup(RENDER_GROUP_CITYBUILD_MENU);

		if (editedReaction == null)
			return;

		// Initialize inputs with default values
		ChatReaction reaction = editedReaction.reaction;
		textTypeInput.select(reaction.regEx ? TextType.REGEX : TextType.TEXT);
		triggerInput.setText(reaction.trigger);
		commandInput.setText(reaction.command);
		compareModeInput.setSelected(reaction.matchAll ? TextCompareMode.EQUALS : TextCompareMode.CONTAINS);
		citybuildInput.setSelected(reaction.citybuild);
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// Draw background
		DrawUtils.drawAutoDimmedBackground(scrollbar.getScrollY());
		scrollbar.draw(mouseX, mouseY);

		GL11.glTranslated(0, scrollbar.getScrollY(), 0);

		// Configure content
		if (textTypeInput.getSelected() == TextType.NONE) {
			cancelButton.yPosition = saveButton.yPosition = (int) textTypeInput.bottom() + 35; // 35px padding (as in initGui)
		} else {
			int bottom;
			if (textTypeInput.getSelected() == TextType.TEXT) {
				triggerInput.label("Text").placeholder("[GrieferUtils] /freekiste ist nun verfügbar!");
				commandInput.placeholder("/gu:run_on_cb /freekiste");
				bottom = (int) compareModeInput.bottom();

				draw(mouseX, mouseY, RENDER_GROUP_TEXT);
			} else {
				triggerInput.label("Regulärer Ausdruck").placeholder("§8^\\[[^ ]+ ┃ ([^ ]+) -> mir] (.*)$");
				commandInput.placeholder("§8/msg MainAccount $1: $2");
				bottom = (int) commandInput.bottom();
			}

			citybuildInput.y(bottom + PADDING);
			cancelButton.yPosition = saveButton.yPosition = (int) citybuildInput.bottom() + PADDING;
			scrollbar.update(getButtonHeight(saveButton) + saveButton.yPosition - HEADER_HEIGHT);
		}
		switch (textTypeInput.getSelected()) {
			case REGEX:
				try {
					Pattern.compile(triggerInput.getText());
				} catch (PatternSyntaxException e) {
					saveButton.enabled = false;
					break;
				}
			case TEXT:
				saveButton.enabled = !triggerInput.getText().isEmpty() && !commandInput.getText().isEmpty();
				break;
			default:
				saveButton.enabled = false;
		}
		mouseY -= scrollbar.getScrollY();

		// Draw content
		draw(mouseX, mouseY);

		if (textTypeInput.getSelected() != TextType.NONE)
			draw(mouseX, mouseY, RENDER_GROUP_SELECTED);

		GL11.glTranslated(0, -scrollbar.getScrollY(), 0);

		// Draw header
		int headerSize = 45;
		DrawUtils.drawOverlayBackground(0, headerSize); // header background
		DrawUtils.drawGradientShadowTop(headerSize, 0.0, this.width); // header gradient

		// Draw currently open addon
		int actionSize = 100;
		DrawUtils.drawString("GrieferUtils", width / 2d - actionSize + 30, 25);
		int addonIconSize = 20;
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("griefer_utils", "icons/icon.png"));
		DrawUtils.drawTexture(width / 2d + actionSize - addonIconSize, 20, 256, 256, addonIconSize, addonIconSize);

		// Draw footer
		int footerSize = 10;
		DrawUtils.drawOverlayBackground(height - footerSize, height); // footer background
		DrawUtils.drawGradientShadowBottom(height - footerSize, 0, width); // footer gradient

		// Draw citybuild menu over footer
		GL11.glTranslated(0, scrollbar.getScrollY(), 0);
		citybuildInput.setScreenHeight(MinecraftUtil.screenHeight() - scrollbar.getScrollY());
		draw(mouseX, mouseY, RENDER_GROUP_CITYBUILD_MENU);
		GL11.glTranslated(0, -scrollbar.getScrollY(), 0);

		// Draw post
		draw(mouseX, (int) (mouseY + scrollbar.getScrollY()), RENDER_GROUP_POST);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		backButton.mousePressed(mouseX, mouseY, mouseButton); // Back button is not affect by scroll

		// Ignore clicks in header
		if (mouseY < HEADER_HEIGHT - 5) // allow click in gradient (5px)
			return;

		scrollbar.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.CLICKED);
		mouseY -= scrollbar.getScrollY();

		// prioritize citybuild as it overlaps with the save and close buttons
		if (textTypeInput.getSelected() != TextType.NONE && citybuildInput.onClick(mouseX, mouseY, mouseButton))
			return;

		for (Clickable clickable : clickables)
			if (clickable != backButton) // Don't handle backButton, as it's already handled
				clickable.mousePressed(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		mouseY -= scrollbar.getScrollY();
		scrollbar.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.RELEASED);
		super.mouseReleased(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
		mouseY -= scrollbar.getScrollY();
		scrollbar.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.DRAGGING);
		super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
	}

	@Override
	public void handleMouseInput() {
		if (citybuildInput.getHoverSelected() == null)
			scrollbar.mouseInput();
		else
			citybuildInput.onScroll();
		super.handleMouseInput();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		if (!triggerInput.isFocused() && !commandInput.isFocused())
			if (keyCode == 1 || typedChar == '\b') // ESC / Back
				close();

		if (typedChar == '\t') {
			// Trigger -> Command
			if (triggerInput.isFocused()) {
				triggerInput.setFocused(false);
				commandInput.setFocused(true);
			}

			// Command -> Citybuild / Compare mode
			else if (commandInput.isFocused()) {
				commandInput.setFocused(false);
				if (textTypeInput.getSelected() == TextType.REGEX)
					citybuildInput.onClick(citybuildInput.getX(), citybuildInput.getY(), 0); // Use onClick so scrollbar is initialized
				else
					compareModeInput.setOpen(true);
			}

			// Compare mode -> Citybuild
			else if (compareModeInput.isOpen()) {
				compareModeInput.setOpen(false);
				citybuildInput.onClick(citybuildInput.getX(), citybuildInput.getY(), 0); // Use onClick so scrollbar is initialized
			}

			// Citybuild -> Trigger
			else if (citybuildInput.isOpen()) {
				citybuildInput.setOpen(false);
				triggerInput.setFocused(true);
			}
		}
		super.keyTyped(typedChar, keyCode);
	}

	private void close() {
		EventRegisterer.unregister(this);
		backgroundScreen.initGui();
		Minecraft.getMinecraft().displayGuiScreen(backgroundScreen);
	}

	private void save() {
		ChatReaction reaction = editedReaction != null ? editedReaction.reaction : new ChatReaction();
		reaction.regEx = textTypeInput.getSelected() == TextType.REGEX;
		reaction.matchAll = compareModeInput.getSelected() == TextCompareMode.EQUALS;
		reaction.trigger = triggerInput.getText();
		reaction.command = commandInput.getText();
		reaction.citybuild = citybuildInput.getSelected();
		reaction.completed = true;

		if (editedReaction == null) {
			reaction.enabled = true;

			// Add reaction
			new ReactionDisplaySetting(reaction, (SettingsElement) FileProvider.getSingleton(ChatReactor.class).getMainElement())
				.icon(textTypeInput.getSelected().getIcon());
		}

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

	private enum TextCompareMode implements Named {

		CONTAINS("Wenn die Nachricht den Text beinhaltet"), EQUALS("Wenn die Nachricht dem Text entspricht");

		private final String name;

		TextCompareMode(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

	}

}