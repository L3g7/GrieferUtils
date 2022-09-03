package dev.l3g7.griefer_utils.settings.elements.keysetting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.misc.Config;
import dev.l3g7.griefer_utils.misc.JsonBuilder;
import dev.l3g7.griefer_utils.settings.elements.SettingElementBuilder;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.utils.Consumer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class KeySetting extends KeyElement implements SettingElementBuilder<KeySetting> {

	private final List<Runnable> callbacks = new ArrayList<>();
	private final ArrayList<Integer> pressedKeys = new ArrayList<>();
	private String configKey = null;
	private ArrayList<Integer> keys;
	private boolean pressed;
	private boolean triggersInChest = false;

	public KeySetting() {
		super("§cNo name set", null, v -> {});

		Reflection.set(this, (Consumer<ArrayList<Integer>>) v -> {
			keys = v;

			if(configKey != null) {
				Config.set(configKey, JsonBuilder.array(keys.stream().map(JsonPrimitive::new).toArray(JsonElement[]::new)));
				Config.save();
			}
		}, "changeListener");

		MinecraftForge.EVENT_BUS.register(this);
	}

	public ArrayList<Integer> get() {
		return keys;
	}

	public KeySetting set(ArrayList<Integer> v) {
		keys = v;
		setKeys(v);
		return this;
	}

	public KeySetting set(Integer... ints) {
		set(new ArrayList<>(Arrays.asList(ints)));
		return this;
	}

	public KeySetting defaultValue(ArrayList<Integer> defaultValue) {
		if(this.keys == null) {
			set(defaultValue);
		}
		return this;
	}

	public KeySetting defaultValue(Integer... defaultValues) {
		if(this.keys == null) {
			set(defaultValues);
		}
		return this;
	}

	public KeySetting triggersInChest() {
		this.triggersInChest = true;
		return this;
	}

	public KeySetting allowMouseButtons() {
		setAllowMouseButtons(true);
		return this;
	}

	public KeySetting config(String configKey) {
		this.configKey = configKey;
		if (Config.has(configKey))
			set((ArrayList<Integer>) StreamSupport.stream(Config.get(configKey).getAsJsonArray().spliterator(), false)
					.map(JsonElement::getAsInt)
					.collect(Collectors.toList()));
		return this;
	}

	public KeySetting callback(Runnable callback) {
		callbacks.add(callback);
		return this;
	}

	public boolean isPressed() {
		return pressed;
	}

	@SubscribeEvent
	public void onKeyPress(InputEvent.KeyInputEvent event) {
		if (Keyboard.isRepeatEvent() || keys == null)
			return;

		if (mc.currentScreen == null)
			checkForPressedButtons(false);
	}

	@SubscribeEvent
	public void onGuiKeyPress(GuiScreenEvent.KeyboardInputEvent event) {
		if (Keyboard.isRepeatEvent() || keys == null || !triggersInChest)
			return;

		if (mc.currentScreen instanceof GuiContainer)
			checkForPressedButtons(false);
	}

	@SubscribeEvent
	public void onMouse(InputEvent.MouseInputEvent event) {
		if (keys == null || !allowsMouseButtons())
			return;

		if (mc.currentScreen == null)
			checkForPressedButtons(true);
	}

	@SubscribeEvent
	public void onGuiMouse(GuiScreenEvent.MouseInputEvent event) {
		if (keys == null || !allowsMouseButtons())
			return;

		if (mc.currentScreen instanceof GuiContainer)
			checkForPressedButtons(true);
	}

	private void checkForPressedButtons(boolean mouse) {
		boolean isPressed = mouse ? Mouse.getEventButtonState() : Keyboard.getEventKeyState();
		Integer pressedKey = mouse ? Mouse.getEventButton() - 100 : Keyboard.getEventKey();

		// If the pressed key isn't actually one that is searched for, return
		if (!keys.contains(pressedKey))
			return;

		if (isPressed)
			pressedKeys.add(pressedKey);
		else
			pressedKeys.remove(pressedKey);

		if (keys.isEmpty() || !isPressed)
			return;

		// Trigger callbacks if all keys are pressed
		pressed = true;
		for (Integer key : keys) {
			// I tried using Mouse.isButtonDown / Keyboard.isKeyDown, but when all keys are pressed in the same tick,
			// the key is pressed before the event is being sent, causing the callbacks to be triggered once for each button.
			pressed &= pressedKeys.contains(key);
		}

		if (pressed)
			callbacks.forEach(Runnable::run);
	}

}
