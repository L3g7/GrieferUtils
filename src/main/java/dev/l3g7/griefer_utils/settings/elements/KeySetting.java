package dev.l3g7.griefer_utils.settings.elements;

import dev.l3g7.griefer_utils.misc.Config;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.settings.elements.KeyElement;
import net.labymod.utils.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class KeySetting extends KeyElement implements SettingElementBuilder<KeySetting> {

	private final List<Runnable> callbacks = new ArrayList<>();
	private String configKey = null;
	private Integer currentValue;
	private boolean pressed;

	public KeySetting() {
		super("§cNo name set", null, 0, v -> {});

		Reflection.set(this, (Consumer<Integer>) v -> {
			currentValue = v;

			if(this.configKey != null) {
				Config.set(this.configKey, v);
				Config.save();
			}
		}, "changeListener");

		setAllowMouseButtons(true);
		MinecraftForge.EVENT_BUS.register(this);
	}

	public Integer get() {
		return currentValue;
	}

	public KeySetting set(Integer v) {
		currentValue = v;
		Reflection.set(this, v, "currentKey");
		Reflection.invoke(this, "updateValue");
		return this;
	}

	public KeySetting defaultValue(Integer defaultValue) {
		if(this.currentValue == null) {
			set(defaultValue);
		}
		return this;
	}

	public KeySetting config(String configKey) {
		this.configKey = configKey;
		if (Config.has(configKey)) {
			if(Config.get(configKey).isJsonNull())
				set(null);
			else
				set(Config.get(configKey).getAsInt());
		}
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
		if(Keyboard.isRepeatEvent())
			return;

		if(Minecraft.getMinecraft().currentScreen == null || Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
			if (currentValue == Keyboard.getEventKey()) {
				// Only run callback if the state was changed
				if(pressed == Keyboard.getEventKeyState())
					return;

				pressed = Keyboard.getEventKeyState();
				if(pressed)
					callbacks.forEach(Runnable::run);
			}
		}
	}

}
