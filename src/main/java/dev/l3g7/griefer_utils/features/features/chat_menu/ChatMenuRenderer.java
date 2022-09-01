package dev.l3g7.griefer_utils.features.features.chat_menu;

import net.labymod.main.LabyMod;
import net.labymod.utils.DrawUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

import java.util.List;

import static dev.l3g7.griefer_utils.features.Feature.mc;

public class ChatMenuRenderer {

	private final List<ChatMenuEntry> entries;
	private final Minecraft mc;
	private final String playerName;
	private final String titleText;
	private final DrawUtils utils = new DrawUtils();

	private int boxHeight;
	private final int boxWidth;
	private int x;
	private int y;
	private int hoveredEntry = -1;


	public ChatMenuRenderer(List<ChatMenuEntry> entries, String playerName) {
		this.entries = entries;
		this.playerName = playerName;
		this.titleText = "ChatMenü §a" + playerName;
		this.mc = Minecraft.getMinecraft();

		// Box size
		boxHeight = 16 + 15 * entries.size();
		boxWidth = Math.max(150, getWidth(titleText) + 16);

		if (!entries.isEmpty())
			boxHeight += 5;

		// Box position (upper left corner)
		x = getMouseX();
		y = getMouseY();

		// Make sure the box in inside the window
		if (utils.getWidth() - x < boxWidth)
			x = utils.getWidth() - boxWidth;

		if (utils.getHeight() - y < boxHeight)
			y = utils.getHeight() - boxHeight;
	}

	private void drawString(String text, float x, float y) {
		utils.getFontRenderer().drawString(text, x, y, 0xFFFFFFFF, false);
	}

	private int getWidth(String text) {
		return utils.getFontRenderer().getStringWidth(text);
	}

	public void render() {
		// Draw the box
		DrawUtils.drawRect(x, y, x + boxWidth, y + 16, 0xFF000000);
		DrawUtils.drawRect(x, y + 16, x + boxWidth, y + boxHeight, 0xFF060606);

		// Draw the entries
		float currentY = y;
		drawString(titleText, x + (boxWidth - getWidth(titleText)) / 2f, currentY += 4);
		currentY += 3;
		for (int i = 0; i < entries.size(); i++) {
			ChatMenuEntry entry = entries.get(i);
			String name = entry.getName();

			if (getWidth(name) > boxWidth - 24) {
				while (getWidth(name + "...") > boxWidth - 24)
					name = name.substring(0, name.length() - 1);
				name = name + "...";
			}

			drawString(name, x + 21, currentY += 15);

			// Draw icons (Copied from GuiChatCustom)
			if (entry.getIcon().hasMaterialIcon()) {
				GlStateManager.pushMatrix();
				GlStateManager.scale(0.75, 0.75, 1.0);
				double posMultiplier = 1 / 0.75;
				LabyMod.getInstance().getDrawUtils().renderItemIntoGUI(entry.getIcon().getMaterialIcon().createItemStack(), (x + 4) * posMultiplier, (currentY - 2) * posMultiplier);
				GlStateManager.popMatrix();
			} else if (entry.getIcon().hasTextureIcon()) {
				mc.getTextureManager().bindTexture(entry.getIcon().getTextureIcon());
				utils.drawTexture(x + 4, currentY - 2, 256, 256, 12, 12);
			}

			// Draw frame if hovered
			if (i == hoveredEntry)
				utils.drawRectBorder(x + 2, currentY - 4, x + boxWidth - 4, currentY + 12, 0xFF00FF00, 1);
		}
	}

	public boolean onMouse() {
		hoveredEntry = -1;

		if (outOfBox())
			return false;

		int mouseY = getMouseY();

		for (int i = 0; i < entries.size(); i++) {
			int boxStart = y + 19 + 15 * i;
			int boxEnd = boxStart + 14;

			if (boxStart <= mouseY && mouseY <= boxEnd) {
				hoveredEntry = i;
				break;
			}
		}

		if (hoveredEntry == -1 || !Mouse.getEventButtonState() || Mouse.getEventButton() != 0)
			return false;

		// Trigger the consumer and close the gui
		entries.get(hoveredEntry).getConsumer().accept(playerName);
		return true;
	}

	public boolean outOfBox() {
		int mouseX = getMouseX();
		int mouseY = getMouseY();
		return mouseX < x || mouseY < y || mouseX > x + boxWidth || mouseY > y + boxHeight;
	}

	public static int getMouseX() {
		return Mouse.getX() * new DrawUtils().getWidth() / mc().displayWidth;
	}

	public static int getMouseY() {
		DrawUtils drawUtils = new DrawUtils();
		return drawUtils.getHeight() - Mouse.getY() * drawUtils.getHeight() / mc().displayHeight - 1;
	}
}
