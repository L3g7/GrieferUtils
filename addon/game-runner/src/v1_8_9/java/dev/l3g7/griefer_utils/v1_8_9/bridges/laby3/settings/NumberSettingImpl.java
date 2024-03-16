package dev.l3g7.griefer_utils.v1_8_9.bridges.laby3.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.laby3.settings.Laby3Setting;
import dev.l3g7.griefer_utils.settings.types.NumberSetting;
import net.labymod.gui.elements.ModTextField;
import net.labymod.settings.elements.NumberElement;

public class NumberSettingImpl extends NumberElement implements Laby3Setting<NumberSetting, Integer>, NumberSetting {

	private final ExtendedStorage<Integer> storage = new ExtendedStorage<>(JsonPrimitive::new, JsonElement::getAsInt, 0);

	public NumberSettingImpl() {
		super("§cNo name set", null, 0);
		addCallback(this::set);
	}

	@Override
	public ExtendedStorage<Integer> getStorage() {
		return storage;
	}

	@Override
	public NumberSetting placeholder(String placeholder) {
		ModTextField textField = Reflection.get(this, "textField");
		textField.setPlaceHolder(placeholder);
		return this;
	}

	@Override
	public NumberSetting min(int min) {
		setMinValue(min);
		set(getCurrentValue());
		return this;
	}

	@Override
	public NumberSetting max(int max) {
		setMaxValue(max);
		set(getCurrentValue());
		return this;
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		drawIcon(x, y);
	}

}
