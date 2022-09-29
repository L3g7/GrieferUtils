package dev.l3g7.griefer_utils.settings.elements;

import net.labymod.main.LabyMod;
import net.labymod.main.ModTextures;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.DrawUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;

public class SmallButtonSetting extends ControlElement implements SettingElementBuilder<SmallButtonSetting> {

	private final GuiButton button = new GuiButton(-2, 0, 0, 23, 20, "");
	private final List<Runnable> callbacks = new ArrayList<>();
	private IconData buttonIcon = new IconData(ModTextures.BUTTON_ADVANCED);

	public SmallButtonSetting() {
		super("§cNo name set", null);
		setSettingEnabled(false);
	}

	public SmallButtonSetting callback(Runnable runnable) {
		callbacks.add(runnable);
		return this;
	}

	public SmallButtonSetting buttonIcon(IconData icon) {
		buttonIcon = icon;
		return this;
	}

	@Override
	public int getObjectWidth() {
		return 0;
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (!button.mousePressed(mc, mouseX, mouseY))
			return;

		button.playPressSound(mc.getSoundHandler());
		callbacks.forEach(Runnable::run);
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);

		mouseOver = mouseX > x && mouseX < maxX && mouseY > y && mouseY < maxY;

		button.xPosition = maxX - 23 - 2;
		button.yPosition = y + 1;
		button.drawButton(mc, mouseX, mouseY);

		// Draw file icon
		DrawUtils drawUtils = LabyMod.getInstance().getDrawUtils();

		if (buttonIcon == null || buttonIcon.hasMaterialIcon() == buttonIcon.hasTextureIcon()) // If no material and no texture exists (You can't have both)
			return;

		if (buttonIcon.hasMaterialIcon()) {
			drawUtils.drawItem(buttonIcon.getMaterialIcon().createItemStack(), button.xPosition + 3, button.yPosition + 2, null);
			return;
		}

		GlStateManager.enableBlend();
		GlStateManager.color(1, 1, 1);
		Minecraft.getMinecraft().getTextureManager().bindTexture(buttonIcon.getTextureIcon());
		drawUtils.drawTexture(button.xPosition + 4, button.yPosition + 3, 0, 0, 256, 256, 14, 14, 2);
	}
}