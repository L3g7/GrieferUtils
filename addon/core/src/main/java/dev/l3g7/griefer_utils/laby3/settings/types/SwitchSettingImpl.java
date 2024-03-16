package dev.l3g7.griefer_utils.laby3.settings.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.laby3.settings.Laby3Setting;
import dev.l3g7.griefer_utils.laby4.settings.types.DropDownSettingImpl;
import dev.l3g7.griefer_utils.laby4.settings.types.KeySettingImpl;
import dev.l3g7.griefer_utils.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.KeySetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import net.labymod.settings.elements.BooleanElement;
import net.labymod.utils.Consumer;

import static dev.l3g7.griefer_utils.settings.types.SwitchSetting.TriggerMode.HOLD;

public class SwitchSettingImpl extends BooleanElement implements Laby3Setting<SwitchSetting, Boolean>, SwitchSetting {

	private final ExtendedStorage<Boolean> storage = new ExtendedStorage<>(JsonPrimitive::new, JsonElement::getAsBoolean, false);
	private TriggerMode previousMode; // NOTE: refactor

	public SwitchSettingImpl() {
		super("§cNo name set", null, v -> {}, false);
		custom("An", "Aus");
		setSettingEnabled(true);
		Reflection.set(this, "toggleListener", (Consumer<Boolean>) this::set);
	}

	@Override
	public SwitchSetting asCheckbox() {
		return this; // TODO noop
	}

	@Override
	public SwitchSetting addHotkeySetting(String whatActivates, TriggerMode defaultTriggerMode) {
		DropDownSettingImpl<TriggerMode> triggerMode = (DropDownSettingImpl<TriggerMode>) DropDownSetting.create(TriggerMode.class)
			.name("Auslösung")
			.icon("lightning")
			.inferConfig("triggerMode")
			.defaultValue(defaultTriggerMode)
			.callback(m -> {
				if (previousMode != null && previousMode != m)
					SwitchSettingImpl.this.set(false);

				previousMode = m;
			});

		if (defaultTriggerMode != null) {
			addSetting(0, HeaderSetting.create());
			addSetting(0, triggerMode);
		}

		KeySettingImpl key = (KeySettingImpl) KeySetting.create()
			.name("Taste")
			.icon("key")
			.inferConfig("key")
			.pressCallback(p -> {
				if (p || triggerMode.get() == HOLD)
					this.set(!this.get());
			});
		addSetting(0, key);

		key.description("Welche Taste " + whatActivates + " aktiviert.");
		triggerMode.description("Halten: Aktiviert " + whatActivates + ", während die Taste gedrückt wird.",
			"Umschalten: Schaltet " + whatActivates + " um, wenn die Taste gedrückt wird.");
		return this;
	}

	@Override
	public ExtendedStorage<Boolean> getStorage() {
		return storage;
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		drawIcon(x, y);
	}

}
