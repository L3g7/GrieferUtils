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

package dev.l3g7.griefer_utils.features.chat.chat_menu;

import dev.l3g7.griefer_utils.core.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.features.chat.chat_menu.ChatMenuEntry.Action;
import dev.l3g7.griefer_utils.features.chat.chat_menu.ChatMenuEntry.IconType;
import dev.l3g7.griefer_utils.misc.gui.elements.*;
import dev.l3g7.griefer_utils.util.AddonUtil;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.gui.elements.Scrollbar;
import net.labymod.settings.LabyModModuleEditorGui;
import net.labymod.settings.PreviewRenderer;
import net.labymod.settings.elements.ControlElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AddChatMenuEntryGui extends Gui {

	private static final int RENDER_GROUP_POST = 1; // Drawable objects rendered after everything else
	private static final int RENDER_GROUP_SELECTED = 2; // Rendered if a type is selected
	private static final int RENDER_GROUP_ITEM_ICON = 3; // Rendered if the icon type "item" is selected
	private static final int RENDER_GROUP_ITEM_ICON_MENU = 4;
	private static final int RENDER_GROUP_FILE_ICON = 5; // Rendered if the icon type "file" is selected

	private static final int PADDING = 35; // 35px between inputs
	private static final int HEADER_HEIGHT = 50;

	private final GuiScreen backgroundScreen;
	private final EntryDisplaySetting editedEntry;

	// Input fields
	private SelectButtonGroup<Action> actionTypeInput;
	private TextField actionInput;
	private TextField nameInput;
	private SelectButtonGroup<IconType> iconInput;
	private DropDown<ItemStack> itemIconInput;
	private ImageSelection fileIconInput;

	private Scrollbar scrollbar;
	private Button backButton;
	private Button cancelButton;
	private Button saveButton;

	public AddChatMenuEntryGui(EntryDisplaySetting entry, GuiScreen backgroundScreen) {
		this.backgroundScreen = backgroundScreen;
		editedEntry = entry;
		EventRegisterer.register(this);
	}

	public void initGui() {
		super.initGui();
		backgroundScreen.width = width;
		backgroundScreen.height = height;
		if (backgroundScreen instanceof LabyModModuleEditorGui)
			PreviewRenderer.getInstance().init(AddChatMenuEntryGui.class);

		int center = width / 2;
		int top = HEADER_HEIGHT + 80; // 80px for breadcrumb

		// Header and breadcrumb
		createCenteredText("§e§l" + Constants.ADDON_NAME, 1.3).pos(center, 81);
		createCenteredText("§e§lChatmenü", .7).pos(center, 105);
		backButton = createButton("<") // Back button
			.pos(center - 100, 20)
			.size(22, 20)
			.renderGroup(RENDER_GROUP_POST)
			.callback(this::close);

		// cancel and save button
		int buttonWidth = 100;
		cancelButton = createButton(editedEntry == null ? "Abbrechen" : "Löschen")
			.x(center - buttonWidth - 5) // 2 * 5px padding between buttons
			.size(buttonWidth, 20)
			.callback(() -> {
				if (editedEntry != null)
					editedEntry.delete();
				close();
			});

		saveButton = createButton(editedEntry == null ? "Hinzufügen" : "Bearbeiten")
			.x(center + 5) // 2 * 5px padding between buttons
			.size(buttonWidth, 20)
			.callback(this::save);

		// Action selection
		actionTypeInput = createSelectGroup(Action.CONSUMER, "Aktion")
			.y(top);

		int width = actionTypeInput.width();
		int x = center - width / 2;

		// Scrollbar
		int guiRightEdge = (this.width - width) / 2 + width;
		scrollbar = new Scrollbar(1);
		scrollbar.setPosition(guiRightEdge + 4, 50, guiRightEdge + 8, this.height - 15);
		scrollbar.setSpeed(20);
		scrollbar.init();

		// Trigger input
		actionInput = createTextField("<Von Auswahl abhängiges Label>")
			.pos(x, actionTypeInput.bottom() + PADDING)
			.width(width)
			.renderGroup(RENDER_GROUP_SELECTED);

		// Command input
		nameInput = createTextField("Befehl")
			.pos(x, actionInput.bottom() + PADDING)
			.width(width)
			.renderGroup(RENDER_GROUP_SELECTED);

		iconInput = createSelectGroup(IconType.DEFAULT, "Icon")
			.y(nameInput.bottom() + PADDING)
			.renderGroup(RENDER_GROUP_SELECTED);

		width = iconInput.width();

		itemIconInput = createDropDown(new ItemStack(Blocks.stone), ItemUtil.ALL_ITEMS, "")
			.y(iconInput.bottom())
			.width(width)
			.renderGroup(RENDER_GROUP_ITEM_ICON)
			.menuRenderGroup(RENDER_GROUP_ITEM_ICON_MENU);

		fileIconInput = createImageSelection("")
			.pos((this.width - width) / 2d, iconInput.bottom())
			.width(width)
			.renderGroup(RENDER_GROUP_FILE_ICON);

		if (editedEntry == null)
			return;

		// Initialize inputs with default values
		ChatMenuEntry entry = editedEntry.entry;
		nameInput.setText(entry.name);
		actionTypeInput.select(entry.action);
		actionInput.setText((String) entry.command);
		iconInput.select(entry.iconType);
		switch (entry.iconType) {
			case ITEM:
				itemIconInput.setSelected((ItemStack) entry.icon);
				break;
			case IMAGE_FILE:
				fileIconInput.select((File) entry.icon);
				break;
		}
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// Draw background
		drawUtils.drawAutoDimmedBackground(scrollbar.getScrollY());
		scrollbar.draw(mouseX, mouseY);

		GL11.glTranslated(0, scrollbar.getScrollY(), 0);

		// Configure content
		saveButton.enabled = !actionInput.getText().isEmpty() && !nameInput.getText().isEmpty();

		if (actionTypeInput.getSelected() == Action.CONSUMER)
			cancelButton.yPosition = saveButton.yPosition = (int) actionTypeInput.bottom() + PADDING;
		else {
			switch (actionTypeInput.getSelected()) {
				case OPEN_URL:
					actionInput.label("URL").placeholder("https://namemc.com/search?q=%name%");
					nameInput.placeholder("NameMC öffnen");
					break;
				case RUN_CMD:
					actionInput.label("Befehl").placeholder("/startkick %name%");
					nameInput.placeholder("Spieler kicken");
					break;
				case SUGGEST_CMD:
					actionInput.label("Befehl").placeholder("§8/msg %name%");
					nameInput.placeholder("MSG an Spieler");
					break;
			}

			List<Button> buttons = Reflection.get(iconInput, "buttons");
			Reflection.set(buttons.get(0), new ControlElement.IconData("griefer_utils/icons/" + actionTypeInput.getSelected().getIcon() + ".png"), "icon");
			double bottom;
			switch (iconInput.getSelected()) {
				case ITEM:
					bottom = itemIconInput.bottom();
					break;
				case IMAGE_FILE:
					bottom = fileIconInput.bottom();
					saveButton.enabled &= fileIconInput.getSelection() != null;
					break;
				default:
					bottom = iconInput.bottom();
			}
			cancelButton.yPosition = saveButton.yPosition = (int) bottom + PADDING;
			scrollbar.update(saveButton.height + saveButton.yPosition - HEADER_HEIGHT);
		}

		mouseY -= scrollbar.getScrollY();

		// Draw content
		draw(mouseX, mouseY);

		if (actionTypeInput.getSelected() != Action.CONSUMER)
			draw(mouseX, mouseY, RENDER_GROUP_SELECTED);

		if (iconInput.getSelected() == IconType.ITEM)
			draw(mouseX, mouseY, RENDER_GROUP_ITEM_ICON);
		else if (iconInput.getSelected() == IconType.IMAGE_FILE)
			draw(mouseX, mouseY, RENDER_GROUP_FILE_ICON);

		GL11.glTranslated(0, -scrollbar.getScrollY(), 0);

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

		// Draw citybuild menu over footer
		GL11.glTranslated(0, scrollbar.getScrollY(), 0);
		itemIconInput.setScreenHeight(MinecraftUtil.screenHeight() - scrollbar.getScrollY());
		draw(mouseX, mouseY, RENDER_GROUP_ITEM_ICON_MENU);
		GL11.glTranslated(0, -scrollbar.getScrollY(), 0);

		// Draw post
		draw(mouseX, (int) (mouseY + scrollbar.getScrollY()), RENDER_GROUP_POST);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		backButton.mousePressed(mouseX, mouseY, mouseButton); // Back button is not affect by scroll

		// Ignore clicks in header
		if (mouseY < HEADER_HEIGHT - 5) // allow click in gradient (5px)
			return;

		scrollbar.mouseAction(mouseX, mouseY, Scrollbar.EnumMouseAction.CLICKED);
		mouseY -= scrollbar.getScrollY();

		// prioritize cityBuild as it overlaps with the save and close buttons
		if (iconInput.getSelected() == IconType.ITEM && itemIconInput.onClick(mouseX, mouseY, mouseButton))
			return;

		for (Clickable clickable : clickables)
			if (clickable != backButton // Don't handle backButton, as it's already handled
			&& clickable != itemIconInput) // Don't handle itemIconInput, as it's already handled
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
	public void handleMouseInput() throws IOException {
		if (itemIconInput.getHoverSelected() == null)
			scrollbar.mouseInput();
		else
			itemIconInput.onScroll();
		super.handleMouseInput();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (!actionInput.isFocused() && !nameInput.isFocused())
			if (keyCode == 1 || typedChar == '\b') // ESC / Back
				close();

		if (typedChar == '\t') {
			// Trigger -> Command
			if (actionInput.isFocused()) {
				actionInput.setFocused(false);
				nameInput.setFocused(true);
			}

			// Command -> Trigger / Item icon
			else if (nameInput.isFocused()) {
				nameInput.setFocused(false);
				if (iconInput.getSelected() == IconType.ITEM)
					itemIconInput.onClick(itemIconInput.getX(), itemIconInput.getY(), 0); // Use onClick so scrollbar is initialized
				else
					actionInput.setFocused(true);
			}

			// CityBuild -> Trigger
			else if (itemIconInput.isOpen()) {
				itemIconInput.setOpen(false);
				actionInput.setFocused(true);
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
		ChatMenuEntry entry = editedEntry != null ? editedEntry.entry : new ChatMenuEntry();
		entry.name = nameInput.getText();
		entry.action = actionTypeInput.getSelected();
		entry.command = actionInput.getText();
		entry.iconType = iconInput.getSelected();
		entry.icon = iconInput.getSelected() == IconType.ITEM ? itemIconInput.getSelected() : fileIconInput.getSelection();
		entry.completed = true;

		if (editedEntry == null)
			// Add entry
			new EntryDisplaySetting(entry, FileProvider.getSingleton(ChatMenu.class).getMainElement());

		close();
	}

}