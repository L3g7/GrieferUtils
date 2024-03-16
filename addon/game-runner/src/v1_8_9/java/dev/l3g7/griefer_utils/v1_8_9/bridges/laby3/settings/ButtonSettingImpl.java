package dev.l3g7.griefer_utils.v1_8_9.bridges.laby3.settings;

import com.google.gson.JsonNull;
import dev.l3g7.griefer_utils.laby3.settings.Icon;
import dev.l3g7.griefer_utils.laby3.settings.Laby3Setting;
import dev.l3g7.griefer_utils.settings.types.ButtonSetting;
import net.labymod.settings.elements.ControlElement;
import net.minecraft.client.gui.GuiButton;

public class ButtonSettingImpl extends ControlElement implements Laby3Setting<ButtonSetting, Object>, ButtonSetting {

	private final ExtendedStorage<Object> storage = new ExtendedStorage<>(e -> JsonNull.INSTANCE, e -> NULL, NULL);
	private final GuiButton button = new GuiButton(-2, 0, 0, 23, 20, "");

	private Icon buttonIcon;

	public ButtonSettingImpl() {
		super("§cNo name set", null);
		setSettingEnabled(false);
	}

	@Override
	public ExtendedStorage<Object> getStorage() {
		return storage;
	}

	@Override
	public ButtonSetting buttonIcon(Object icon) {
		buttonIcon = Icon.of(icon);
		button.displayString = "";
		return this;
	}

	@Override
	public ButtonSetting buttonLabel(String label) {
		buttonIcon = null;
		button.displayString = label; // TODO fix button width
		return this;
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (!button.mousePressed(mc, mouseX, mouseY))
			return;

		button.playPressSound(mc.getSoundHandler());
		notifyChange();
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		drawIcon(x, y);

		mouseOver = mouseX > x && mouseX < maxX && mouseY > y && mouseY < maxY;

		button.xPosition = maxX - 23 - 2;
		button.yPosition = y + 1;
		button.drawButton(mc, mouseX, mouseY);

		if (buttonIcon != null)
			buttonIcon.draw(button.xPosition + 3, button.yPosition + 2);
	}

}
