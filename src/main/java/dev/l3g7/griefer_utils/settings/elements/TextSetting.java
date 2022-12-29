package dev.l3g7.griefer_utils.settings.elements;

import net.labymod.main.LabyMod;
import net.labymod.utils.DrawUtils;
import net.minecraft.client.Minecraft;

public class TextSetting extends HeaderSetting {

	private static final int FONT_HEIGHT = Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
	private double textSize = 1;

	public TextSetting() {
		this("§c");
	}

	public TextSetting(String name) {
		super(name);
	}

	public HeaderSetting scale(double scale) {
		textSize = scale;
		return this;
	}

	@Override
	public int getEntryHeight() {
		return super.getEntryHeight() + (displayName.split("\n").length - 1) * (FONT_HEIGHT + 1);
	}

	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		String text = displayName;
		displayName = null;
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		displayName = text;
		DrawUtils drawUtils = LabyMod.getInstance().getDrawUtils();

		int maxWidth = 0;
		for (String line : displayName.split("\n"))
			maxWidth = Math.max(maxWidth, drawUtils.getStringWidth(line));

		for (String line : displayName.split("\n")) {
			LabyMod.getInstance().getDrawUtils().drawString(line, x + (maxX - x) / 2d - (double) (maxWidth / 2), y + 7, textSize);
			y += FONT_HEIGHT + 1;
		}
	}

}