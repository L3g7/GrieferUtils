package dev.l3g7.griefer_utils.laby3.settings.types;

import com.google.gson.JsonNull;
import dev.l3g7.griefer_utils.laby3.settings.Laby3Setting;
import dev.l3g7.griefer_utils.settings.types.CategorySetting;
import net.labymod.settings.elements.ControlElement;

public class CategorySettingImpl extends ControlElement implements Laby3Setting<CategorySetting, Object>, CategorySetting {

	private final ExtendedStorage<Object> storage = new ExtendedStorage<>(e -> JsonNull.INSTANCE, e -> NULL, NULL);

	public CategorySettingImpl() {
		super("§cNo name set", null);
		setSettingEnabled(true);
		setHoverable(true);
	}

	@Override
	public int getObjectWidth() {
		return 0;
	}

	@Override
	public ExtendedStorage<Object> getStorage() {
		return storage;
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		drawIcon(x, y);
	}

}
