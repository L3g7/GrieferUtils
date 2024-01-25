package dev.l3g7.griefer_utils.misc.gui.elements;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.FONT_HEIGHT;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;

public class Text implements Drawable {

	private final String text;
	private final boolean centered;

	private final int stringWidth;
	private double x, y;
	private final double size;
	private int renderGroup = 0;

	Text(String text, double size, boolean centered) {
		this.text = text;
		this.stringWidth = drawUtils().getStringWidth(text);
		this.size = size;
		this.centered = centered;
	}

	public Text x(double x) {
		this.x = x;
		return this;
	}

	public Text y(double y) {
		this.y = y;
		return this;
	}

	public Text pos(double x, double y) {
		return x(x).y(y);
	}

	public double height() {
		return FONT_HEIGHT * size;
	}

	public double width() {
		return stringWidth * size;
	}

	public Text renderGroup(int group) {
		renderGroup = group;
		return this;
	}

	@Override
	public void draw(int mouseX, int mouseY, int renderGroup) {
		if (this.renderGroup != renderGroup)
			return;

		if (centered)
			drawUtils().drawCenteredString(text, x, y, size);
		else
			drawUtils().drawString(text, x, y, size);
	}

}