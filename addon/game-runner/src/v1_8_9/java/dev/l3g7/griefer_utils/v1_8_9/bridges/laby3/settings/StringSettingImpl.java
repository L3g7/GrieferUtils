package dev.l3g7.griefer_utils.v1_8_9.bridges.laby3.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.api.misc.functions.Predicate;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.laby3.settings.Laby3Setting;
import dev.l3g7.griefer_utils.settings.types.StringSetting;
import net.labymod.gui.elements.ModTextField;
import net.labymod.settings.elements.StringElement;
import net.labymod.utils.Consumer;

public class StringSettingImpl extends StringElement implements Laby3Setting<StringSetting, String>, StringSetting {

	private final ExtendedStorage<String> storage = new ExtendedStorage<>(JsonPrimitive::new, JsonElement::getAsString, "");

	public StringSettingImpl() {
		super("§cNo name set", null, "", v -> {});
		setSettingEnabled(true);
		Reflection.set(this, "changeListener", (Consumer<String>) value -> {
			getStorage().value = value;
			getStorage().callbacks.forEach(c -> c.accept(value));
		});
	}

	@Override
	public ExtendedStorage<String> getStorage() {
		return storage;
	}

	@Override
	public StringSettingImpl maxLength(int maxLength) {
		ModTextField textField = Reflection.get(this, "textField");
		textField.setMaxStringLength(maxLength);
		return this;
	}

	@Override
	public StringSetting placeholder(String placeholder) {
		ModTextField textField = Reflection.get(this, "textField");
		textField.setPlaceHolder(placeholder);
		return this;
	}

	@Override
	public StringSetting validator(Predicate<String> validator) {
		return this; // TODO
	}

	@Override
	public StringSetting moveCursorToEnd() {
		ModTextField textField = Reflection.get(this, "textField");
		textField.setCursorPositionEnd();
		textField.setSelectionPos(textField.getCursorPosition());
		return this;
	}

}
