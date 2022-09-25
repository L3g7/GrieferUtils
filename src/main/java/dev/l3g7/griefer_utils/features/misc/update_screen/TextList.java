package dev.l3g7.griefer_utils.features.misc.update_screen;

import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiListExtended;

import java.util.ArrayList;
import java.util.List;

public class TextList extends GuiListExtended {

	private static final int MAX_LENGTH = 393;

	private final List<Line> lines = new ArrayList<>();
	private final FontRenderer fontRenderer;
	private int textWidth = 0;

	public TextList(Minecraft mc, int width, int height, int top, int bottom, FontRenderer fontRenderer) {
		super(mc, width, height, top, bottom, fontRenderer.FONT_HEIGHT + 1);
		this.fontRenderer = fontRenderer;
	}

	public void addEntry(String text) {
		int stringWidth = fontRenderer.getStringWidth(text);

		if (stringWidth <= MAX_LENGTH) {
			lines.add(new Line(text));
			textWidth = Math.max(textWidth, stringWidth);
			return;
		}

		while (stringWidth > MAX_LENGTH) {

			String partString;

			// Limit the string to a width of 393
			for (int i = text.length(); true; i--) {
				partString = text.substring(0, i);

				if (fontRenderer.getStringWidth(partString) <= MAX_LENGTH)
					break;
			}

			// Chop up string at space
			int i = partString.length() - 1;
			while (true) {
				if (i < 0) {
					i = partString.length() - 1;
					break;
				}

				if (partString.charAt(i) == ' ')
					break;

				i--;
			}

			// Add the string and continue with the rest
			lines.add(new Line(text.substring(0, i)));

			text = text.substring(i + 1);

			stringWidth = fontRenderer.getStringWidth(text);
		}

		lines.add(new Line(text));
		textWidth = getMaxStringWidth();
	}

	private int getMaxStringWidth() {
		return 450;
	}

	@Override
	protected int getScrollBarX() {
		return width - 24;
	}

	@Override
	public int getListWidth() {
		return textWidth;
	}

	public void addEntries(String multiLineText) {
		for (String s : multiLineText.replace("\r", "").split("\n"))
			addEntry(s);
	}

	public IGuiListEntry getListEntry(int index) {
		return lines.get(index);
	}

	protected int getSize() {
		return lines.size();
	}

	private static class Line implements IGuiListEntry {

		private final String text;

		public Line(String text) {
			this.text = text;
		}

		public void setSelected(int a, int b, int c) {}
		public boolean mousePressed(int a, int b, int c, int d, int e, int f) {return false;}
		public void mouseReleased(int a, int b, int c, int d, int e, int f) {}

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected) {
			LabyMod.getInstance().getDrawUtils().drawString(text, x, y);
		}

	}

}
