package dev.l3g7.griefer_utils.core.bridges.laby3.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.misc.functions.Predicate;
import dev.l3g7.griefer_utils.core.api.misc.functions.Supplier;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.labymod.laby3.settings.Laby3Setting;
import dev.l3g7.griefer_utils.core.settings.types.StringSetting;
import dev.l3g7.griefer_utils.core.bridges.laby3.temp.AddonsGuiWithCustomBackButton;
import net.labymod.gui.elements.ModTextField;
import net.labymod.settings.elements.StringElement;
import net.labymod.utils.Consumer;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static net.minecraft.client.gui.Gui.drawRect;

public class StringSettingImpl extends StringElement implements Laby3Setting<StringSetting, String>, StringSetting {

	private final ExtendedStorage<String> storage = new ExtendedStorage<>(JsonPrimitive::new, JsonElement::getAsString, "");

	private Predicate<String> validator = null;
	private boolean invalid = false;
	private final Supplier<Boolean> closeCheck = () -> !invalid;

	public StringSettingImpl() {
		super("§cNo name set", null, "", v -> {});
		setSettingEnabled(true);
		Reflection.set(this, "changeListener", (Consumer<String>) value -> {
			if (validator != null) {
				boolean failed = !validator.test(value);
				if (invalid != failed) {
					invalid = failed;
					name(invalid ? "§c" + displayName : displayName.substring(2));
				}
			}

			if (invalid)
				return;

			getStorage().value = value;
			getStorage().callbacks.forEach(c -> c.accept(value));
		});
	}

	@Override
	public ExtendedStorage<String> getStorage() {
		return storage;
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);

		if (invalid) {
			int rectX = maxX - 82;
			drawRect(rectX, y, rectX + 80, y + 1, 0xFFFF5555);
			drawRect(rectX, y + 21, rectX + 80, y + 22, 0xFFFF5555);
			drawRect(rectX - 1, y, rectX, y + 22, 0xFFFF5555);
			drawRect(rectX + 80, y, rectX + 81, y + 22, 0xFFFF5555);
		}

		if (validator == null)
			return;

		if (mc.currentScreen instanceof ExpandedStringElementGui)
			return;

		if (!(mc().currentScreen instanceof AddonsGuiWithCustomBackButton))
			mc().displayGuiScreen(new AddonsGuiWithCustomBackButton(null));

		((AddonsGuiWithCustomBackButton) mc().currentScreen).addCheck(closeCheck);
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
		this.validator = validator;
		return this;
	}

	@Override
	public StringSetting moveCursorToEnd() {
		ModTextField textField = Reflection.get(this, "textField");
		textField.setCursorPositionEnd();
		textField.setSelectionPos(textField.getCursorPosition());
		return this;
	}

}
