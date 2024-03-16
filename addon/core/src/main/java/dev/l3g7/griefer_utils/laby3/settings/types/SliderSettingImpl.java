package dev.l3g7.griefer_utils.laby3.settings.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.laby3.settings.Laby3Setting;
import dev.l3g7.griefer_utils.settings.types.SliderSetting;
import net.labymod.settings.elements.SliderElement;

public class SliderSettingImpl extends SliderElement implements Laby3Setting<SliderSetting, Integer>, SliderSetting {

	private final ExtendedStorage<Integer> storage = new ExtendedStorage<>(JsonPrimitive::new, JsonElement::getAsInt, 0);

	public SliderSettingImpl() {
		super("§cNo name set", null, 0);
		addCallback(this::set);
	}

	@Override
	public ExtendedStorage<Integer> getStorage() {
		return storage;
	}

	@Override
	public SliderSetting min(int min) {
		return (SliderSetting) setMinValue(min);
	}

	@Override
	public SliderSetting max(int max) {
		return (SliderSetting) setMaxValue(max);
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		drawIcon(x, y);
	}

}
