package dev.l3g7.griefer_utils.settings.elements;

import net.labymod.main.LabyMod;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.DrawUtils;
import net.minecraft.client.Minecraft;

public class TextSetting extends SettingsElement implements SettingElementBuilder<TextSetting> {

	private static final int FONT_HEIGHT = Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
	private int entryHeight = 22;
	private double textSize;

	public TextSetting() {
		this("§c");
	}

	public TextSetting(String name) {
		this(name, 1.0);
	}

	public TextSetting(String displayName, double textSize) {
		super(displayName, null);
		this.textSize = textSize;
	}

	public TextSetting scale(double scale) {
		this.textSize = scale;
		return this;
	}

	public TextSetting entryHeight(int entryHeight) {
		this.entryHeight = entryHeight;
		return this;
	}

	@Override
	public int getEntryHeight() {
		return entryHeight + (displayName.split("\n").length - 1) * (FONT_HEIGHT + 1);
	}

	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		DrawUtils drawUtils = LabyMod.getInstance().getDrawUtils();

		int maxWidth = 0;
		for (String line : displayName.split("\n"))
			maxWidth = Math.max(maxWidth, drawUtils.getStringWidth(line));

		for (String line : displayName.split("\n")) {
			LabyMod.getInstance().getDrawUtils().drawString(line, x + (maxX - x) / 2d - (double) (maxWidth / 2), y + 7, this.textSize);
			y += FONT_HEIGHT + 1;
		}
	}

	public void drawDescription(int x, int y, int screenWidth) {
	}

	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
	}

	public void keyTyped(char typedChar, int keyCode) {
	}

	public void mouseRelease(int mouseX, int mouseY, int mouseButton) {
	}

	public void mouseClickMove(int mouseX, int mouseY, int mouseButton) {
	}

	public void unfocus(int mouseX, int mouseY, int mouseButton) {
	}
}
