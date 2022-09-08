package dev.l3g7.griefer_utils.settings.elements;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.ValueHolder;
import net.labymod.settings.elements.NumberElement;

public class NumberSetting extends NumberElement implements ElementBuilder<NumberSetting>, ValueHolder<NumberSetting, Integer> {

	private final Storage<Integer> storage = new Storage<>(JsonPrimitive::new, JsonElement::getAsInt);

	public NumberSetting() {
		super("§cNo name set", null, 0);
		addCallback(this::set);
	}

	@Override
	public Storage<Integer> getStorage() {
		return storage;
	}

	public NumberSetting min(int min) {
		return (NumberSetting) setMinValue(min);
	}

	public NumberSetting max(int max) {
		return (NumberSetting) setMaxValue(max);
	}

}
