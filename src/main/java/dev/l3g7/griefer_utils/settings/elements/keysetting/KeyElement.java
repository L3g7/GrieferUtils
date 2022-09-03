package dev.l3g7.griefer_utils.settings.elements.keysetting;

import net.labymod.gui.elements.ModTextField;
import net.labymod.main.lang.LanguageManager;
import net.labymod.settings.LabyModModuleEditorGui;
import net.labymod.settings.PreviewRenderer;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Based on LabyMod's KeyElement.
 */
public class KeyElement extends ControlElement {

	private final Consumer<ArrayList<Integer>> changeListener;
	private ArrayList<Integer> keys = new ArrayList<>();
	private ModTextField textField;
	private boolean allowMouseButtons = false;

	public KeyElement(String displayName, ControlElement.IconData iconData, Consumer<ArrayList<Integer>> changeListener, int... keys) {
		super(displayName, iconData);
		this.changeListener = changeListener;

		for (int key : keys)
			this.keys.add(key);

		createTextField();
	}

	private static void updateValue(ModTextField textField, ArrayList<Integer> keys) {
		if (keys.isEmpty()) {
			textField.setText("NONE");
			return;
		}

		textField.setText(keys.stream().map(KeyElement::toString).collect(Collectors.joining(" + ")));
	}

	private static String toString(int currentKey) {
		try {
			return currentKey < 0 ? "Mouse " + (currentKey + 100) : Keyboard.getKeyName(currentKey);
		} catch (ArrayIndexOutOfBoundsException e) {
			return "<" + currentKey + ">";
		}
	}

	public void createTextField() {
		textField = new ModTextField(-2, mc.fontRendererObj, 0, 0, getObjectWidth() - 5, 20);
		textField.setMaxStringLength(500);
		updateValue(textField, keys);
		textField.setCursorPositionEnd();
		textField.setFocused(false);
	}

	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		if (textField == null)
			return;

		textField.xPosition = maxX - getObjectWidth() + 3;
		textField.yPosition = y + 1;
		textField.drawTextBox();
	}

	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		// Checks if the text field is pressed (Copied from textField.mouseClicked and inverted)
		if (mouseX < textField.xPosition || mouseX >= textField.xPosition + textField.width || mouseY < textField.yPosition || mouseY >= textField.yPosition + textField.height)
			return;

		Minecraft.getMinecraft().displayGuiScreen(new ExpandedKeyElementGui(textField, allowMouseButtons, keys -> {
			setKeys(keys);
			changeListener.accept(keys);
		}));
		textField.setFocused(false);
	}

	public void setAllowMouseButtons(boolean allowMouseButtons) {
		this.allowMouseButtons = allowMouseButtons;
	}

	public void setKeys(ArrayList<Integer> keys) {
		this.keys = keys;
		updateValue(textField, keys);
	}

	public boolean allowsMouseButtons() {
		return allowMouseButtons;
	}

	public int getObjectWidth() {
		return 85;
	}

	private static class ExpandedKeyElementGui extends GuiScreen {

		private final ArrayList<Integer> keys = new ArrayList<>();
		private final ArrayList<Integer> pressedKeys = new ArrayList<>();
		private final GuiScreen backgroundScreen;
		private final Consumer<ArrayList<Integer>> callback;
		private final ModTextField preField;
		private final boolean allowMouseButtons;
		private ModTextField textField;

		public ExpandedKeyElementGui(ModTextField preField, boolean allowMouseButtons, Consumer<ArrayList<Integer>> callback) {
			this.backgroundScreen = Minecraft.getMinecraft().currentScreen;
			this.allowMouseButtons = allowMouseButtons;
			this.callback = callback;
			this.preField = preField;
			MinecraftForge.EVENT_BUS.register(this);
		}

		public void initGui() {
			super.initGui();
			backgroundScreen.width = width;
			backgroundScreen.height = height;
			if (backgroundScreen instanceof LabyModModuleEditorGui)
				PreviewRenderer.getInstance().init(ExpandedKeyElementGui.class);

			textField = new ModTextField(0, mc.fontRendererObj, width / 2 - 150, height / 4 + 45, 300, 20);
			textField.setMaxStringLength(Integer.MAX_VALUE);
			textField.setFocused(true);
			textField.setText(preField.getText());
			textField.setCursorPositionEnd();

			buttonList.add(new GuiButton(1, width / 2 - 50, height / 4 + 85, 100, 20, LanguageManager.translate("button_done")));
		}

		public void drawScreen(int mouseX, int mouseY, float partialTicks) {
			backgroundScreen.drawScreen(mouseX, mouseY, partialTicks);
			drawRect(0, 0, width, height, Integer.MIN_VALUE);
			drawRect(width / 2 - 165, height / 4 + 35, width / 2 + 165, height / 4 + 120, Integer.MIN_VALUE);
			textField.drawTextBox();
			super.drawScreen(mouseX, mouseY, partialTicks);
		}

		protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}

		protected void keyTyped(char typedChar, int keyCode) {}

		@SubscribeEvent
		public void onMouse(GuiScreenEvent.MouseInputEvent.Post event) {
			int mouseButton = Mouse.getEventButton() - 100;

			if (Mouse.getEventButtonState())
				addKeyCode(mouseButton);
			else
				pressedKeys.remove((Integer) mouseButton);
		}

		@SubscribeEvent
		public void onKey(GuiScreenEvent.KeyboardInputEvent event) {
			int keyCode = Keyboard.getEventKey();

			if (keyCode == 1) {
				close();
				return;
			}

			pressedKeys.stream().map(Object::toString).collect(Collectors.joining(" ; "));

			if (Keyboard.getEventKeyState())
				addKeyCode(keyCode);
			else
				pressedKeys.remove(((Integer) keyCode));
		}

		private void addKeyCode(int keyCode) {
			if (!textField.isFocused() || (!allowMouseButtons && keyCode < 0))
				return;

			if (pressedKeys.isEmpty())
				keys.clear();

			pressedKeys.add(keyCode);

			if (!keys.contains(keyCode)) {
				keys.add(keyCode);
				updateValue(textField, keys);
				textField.setCursorPositionEnd();
			}
		}

		public void updateScreen() {
			backgroundScreen.updateScreen();
		}

		protected void actionPerformed(GuiButton button) {
			if (button.id == 1)
				close();
		}

		private void close() {
			if (!textField.getText().equals(preField.getText())) // Only trigger if something changed
				callback.accept(keys);
			MinecraftForge.EVENT_BUS.unregister(this);
			Minecraft.getMinecraft().displayGuiScreen(backgroundScreen);
		}
	}

}
