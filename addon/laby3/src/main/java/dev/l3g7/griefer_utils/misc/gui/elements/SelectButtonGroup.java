package dev.l3g7.griefer_utils.misc.gui.elements;

import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.ModColor;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;

public class SelectButtonGroup<E extends Enum<E> & SelectButtonGroup.Selectable> implements Drawable, Clickable {

	protected static final DrawUtils drawUtils = drawUtils();
	private static final double LABEL_HEIGHT = 9 * 1.2;

	private final List<Button> buttons = new ArrayList<>();
	private final String label;
	private E selected;

	private final int x, width;
	private double y;
	private int renderGroup = 0;

	SelectButtonGroup(E placeholder, String label, int screenWidth) {
		selected = placeholder;
		this.label = label;
		int width = 0;

		// Create buttons
		for (E value : placeholder.getDeclaringClass().getEnumConstants()) {
			if (value.getName() == null || value.getName().isEmpty())
				continue;

			Button button = new IconButton(value);
			buttons.add(button);

			if (width > 0)
				width += 10; // 10 px padding between buttons
			width += button.getButtonWidth();
		}
		this.width = width;

		// Set positions
		x = (screenWidth - width) / 2;
		int buttonX = x;
		for (Button button : buttons) {
			button.x(buttonX);
			buttonX += 10 + button.getButtonWidth();
		}
	}

	public SelectButtonGroup<E> y(double y) {
		this.y = y;
		for (Button button : buttons)
			button.y(y + LABEL_HEIGHT + 5);
		return this;
	}

	public SelectButtonGroup<E> renderGroup(int renderGroup) {
		this.renderGroup = renderGroup;
		for (Button button : buttons)
			button.renderGroup(renderGroup);

		return this;
	}

	public int width() {
		return width;
	}

	public double bottom() {
		return y + LABEL_HEIGHT + 5 + 23;
	}

	public E getSelected() {
		return selected;
	}

	public void select(E value) {
		selected = value;
	}

	@Override
	public void draw(int mouseX, int mouseY, int renderGroup) {
		if (this.renderGroup != renderGroup)
			return;

		drawUtils.drawString(label, x, y, 1.2);
		for (Button button : buttons)
			button.draw(mouseX, mouseY, renderGroup);
	}

	@Override
	public void mousePressed(int mouseX, int mouseY, int mouseButton) {
		for (Button button : buttons)
			button.mousePressed(mouseX, mouseY, mouseButton);
	}

	public interface Selectable {

		String getName();

		String getIcon();

	}

	private class IconButton extends Button {

		private final IconData icon;
		private final E value;

		private IconButton(E value) {
			super(value.getName());
			this.icon = new IconData("griefer_utils/icons/" + value.getIcon() + ".png");
			this.value = value;

			height(23);
			width(drawUtils.getStringWidth(value.getName()) + 30); // 20px icon + 2*5px padding
			callback(() -> selected = value);
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY) {
			int x = xPosition;
			int y = yPosition;
			boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + getButtonWidth() && mouseY < y + height;

			// Draw background
			drawUtils().drawRectangle(x, y, x + getButtonWidth(), y + height, ModColor.toRGB(80, 80, 80, 60));

			// Draw foreground
			drawUtils().bindTexture(icon.getTextureIcon());
			if (hovered || selected == value) {
				drawUtils().drawTexture(x + 2, y + 2, 256, 256, 18, 18);
				drawUtils().drawStringWithShadow(displayString, x + 25, y + 7, 0xFFFFFF);
			} else {
				drawUtils().drawTexture(x + 3, y + 3, 256, 256, 16, 16);
				drawUtils().drawStringWithShadow(displayString, x + 24, y + 7, 0xB4B4B4);
			}
		}

	}
}