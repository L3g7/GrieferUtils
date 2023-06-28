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

package dev.l3g7.griefer_utils.settings.elements;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.util.Util;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.ValueHolder;
import net.labymod.gui.elements.ModTextField;
import net.labymod.main.LabyMod;
import net.labymod.settings.LabyModModuleEditorGui;
import net.labymod.settings.PreviewRenderer;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.KeyElement;
import net.labymod.settings.elements.StringElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A setting holding keybinds.
 * Based on LabyMod's {@link KeyElement}, with support for multiple keys.
 */
public class KeySetting extends ControlElement implements ElementBuilder<KeySetting>, ValueHolder<KeySetting, Set<Integer>> { // , ValueHolder<KeySetting, ArrayList<Integer>>

	private final ModTextField previewField;
	private final List<Consumer<Boolean>> pressCallbacks = new ArrayList<>();
	private boolean pressed;
	private boolean triggersInContainers = false;

	private final IconStorage iconStorage = new IconStorage();
	private final Storage<Set<Integer>> storage = new Storage<>(values -> {
		JsonArray array = new JsonArray();
		values.stream().map(JsonPrimitive::new).forEach(array::add);
		return array;
	},
		e -> new LinkedHashSet<>(
			StreamSupport.stream(e.getAsJsonArray().spliterator(), false)
			.map(JsonElement::getAsInt)
			.collect(Collectors.toList())),
		new TreeSet<>()
	);

	public KeySetting() {
		super("§cNo name set", null);
		previewField = new ModTextField(-2, mc.fontRendererObj, 0, 0, getObjectWidth() - 5, 20);
		previewField.setMaxStringLength(500);
		previewField.setText("NONE");
		previewField.setCursorPositionEnd();
		previewField.setFocused(false);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public Storage<Set<Integer>> getStorage() {
		return storage;
	}

	@Override
	public IconStorage getIconStorage() {
		return iconStorage;
	}

	@Override
	public KeySetting set(Set<Integer> value) {
		previewField.setText(Util.formatKeys(value));
		return ValueHolder.super.set(value);
	}

	public KeySetting pressCallback(Consumer<Boolean> callback) {
		pressCallbacks.add(callback);
		return this;
	}

	public KeySetting triggersInContainers() {
		triggersInContainers = true;
		return this;
	}

	@SubscribeEvent
	public void onKeyPress(GuiScreenEvent.KeyboardInputEvent.Post event) {
		if (Keyboard.isRepeatEvent() || !triggersInContainers)
			return;


		// Trigger callbacks if necessary
		if (get().contains(Keyboard.getEventKey())) {
			if (isPressed())
				pressed = true;
			else if (pressed)
				pressed = false;

			pressCallbacks.forEach(c -> c.accept(pressed));
		}
	}

	@SubscribeEvent
	public void onKeyPress(InputEvent.KeyInputEvent event) {
		if (Keyboard.isRepeatEvent())
			return;

		// Trigger callbacks if necessary
		if (get().contains(Keyboard.getEventKey())) {
			if (isPressed())
				pressed = true;
			else if (pressed)
				pressed = false;

			pressCallbacks.forEach(c -> c.accept(pressed));
		}
	}

	public boolean isPressed() {
		return get().stream().allMatch(Keyboard::isKeyDown);
	}

	/**
	 * Draws the key setting.
	 */
	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		drawIcon(x, y);
		if (previewField == null)
			return;

		previewField.xPosition = maxX - getObjectWidth() + 3;
		previewField.yPosition = y + 1;
		previewField.drawTextBox();
		LabyMod.getInstance().getDrawUtils().drawRectangle(x - 1, y, x, maxY, 0x78787878);
	}

	/**
	 * Intercepts clicks into the preview field and opens a {@link KeySelectionGui}.
	 */
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (!previewField.mouseClicked(mouseX, mouseY, mouseButton))
			return;

		previewField.setFocused(false);
		mc.displayGuiScreen(new KeySelectionGui());
	}

	@Override
	public int getObjectWidth() {
		return 85;
	}

	/**
	 * Based on LabyMod's {@link StringElement.ExpandedStringElementGui}.
	 */
	private class KeySelectionGui extends GuiScreen {

		private final Set<Integer> selectedKeys = new LinkedHashSet<>();
		private final Set<Integer> pressedKeys = new LinkedHashSet<>();
		private final GuiScreen backgroundScreen = Minecraft.getMinecraft().currentScreen;
		private ModTextField selectionField;

		@Override
		public void initGui() {
			super.initGui();
			// init background screen
			backgroundScreen.width = width;
			backgroundScreen.height = height;
			if (backgroundScreen instanceof LabyModModuleEditorGui)
				PreviewRenderer.getInstance().init(KeySelectionGui.class);

			// init selection field
			selectionField = new ModTextField(0, mc.fontRendererObj, width / 2 - 150, height / 2 - 30, 300, 20);
			selectionField.setMaxStringLength(Integer.MAX_VALUE);
			selectionField.setFocused(true);
			selectionField.setText(KeySetting.this.previewField.getText());
			selectionField.setCursorPositionEnd();

			// init buttons
			buttonList.add(new GuiButton(1, width / 2 - 110, height / 2 + 10, 100, 20, "Zurücksetzen"));
			buttonList.add(new GuiButton(2, width / 2 + 10, height / 2 + 10, 100, 20, "Speichern"));
		}

		@Override
		public void drawScreen(int mouseX, int mouseY, float partialTicks) {
			backgroundScreen.drawScreen(mouseX, mouseY, partialTicks);
			drawRect(0, 0, width, height, Integer.MIN_VALUE);
			selectionField.drawTextBox();
			super.drawScreen(mouseX, mouseY, partialTicks);
		}

		@Override
		public void handleKeyboardInput() throws IOException {
			super.handleKeyboardInput();

			int keyCode = Keyboard.getEventKey();

			// check ESC press
			if (keyCode == 1) {
				close();
				return;
			}

			// process selection
			if (Keyboard.getEventKeyState()) {
				if (!selectionField.isFocused())
					return;

				// If no keys have been pressed before, begin new input
				if (pressedKeys.isEmpty())
					selectedKeys.clear();

				pressedKeys.add(keyCode);

				// Add button to keys
				selectedKeys.add(keyCode);
				selectionField.setText(Util.formatKeys(selectedKeys));
				selectionField.setCursorPositionEnd();
			}
			else
				pressedKeys.remove(keyCode);
		}

		@Override
		public void updateScreen() {
			backgroundScreen.updateScreen();
		}

		/**
		 * Processes clicks on the "Done" button.
		 */
		@Override
		protected void actionPerformed(GuiButton button) {
			if (button.id == 1) {
				// Reset button
				selectedKeys.clear();
				pressedKeys.clear();
				selectionField.setText(Util.formatKeys(selectedKeys));
				selectionField.setCursorPositionEnd();
			} else if (button.id == 2)
				// Save button
				close();
		}

		/**
		 * Closes the selection gui and updates the parent setting.
		 */
		private void close() {
			MinecraftForge.EVENT_BUS.unregister(this);
			Minecraft.getMinecraft().displayGuiScreen(backgroundScreen);

			// Only trigger callback if something changed
			if (selectionField.getText().equals(previewField.getText()))
				return;

			set(selectedKeys);
		}
	}

}