package dev.l3g7.griefer_utils.settings.elements;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.ValueHolder;
import net.labymod.settings.elements.BooleanElement;

public class BooleanSetting extends BooleanElement implements ElementBuilder<BooleanSetting>, ValueHolder<BooleanSetting, Boolean> {

	private final Storage<Boolean> storage = new ValueHolder.Storage<>(JsonPrimitive::new, JsonElement::getAsBoolean);

	public BooleanSetting() {
		super("§cNo name set", null, v -> {}, false);
		custom("An", "Aus").setSettingEnabled(true);
		this.addCallback(this::set);
	}

	@Override
	public Storage<Boolean> getStorage() {
		return storage;
	}

}
