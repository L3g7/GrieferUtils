package dev.l3g7.griefer_utils.features.misc.update_screen;

import dev.l3g7.griefer_utils.features.features.chat_menu.ChatMenuEntry;
import dev.l3g7.griefer_utils.settings.elements.SmallButtonSetting;
import dev.l3g7.griefer_utils.util.IOUtil;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.io.IOException;

public class V2InfoScreen extends GuiScreen {

	private final String text;
	private TextList textList;
	private GuiScreen previousScreen = Minecraft.getMinecraft().currentScreen;

	public static void open() {
		IOUtil.request("https://grieferutils.l3g7.dev/v2/release_screen_text")
			.asString(text -> Minecraft.getMinecraft().displayGuiScreen(new V2InfoScreen(text)));
	}

	// Make sure the gui closes to the correct screen
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onGuiOpen(GuiOpenEvent event) {
		if (event.isCanceled() || event.gui instanceof V2InfoScreen)
			return;

		previousScreen = event.gui;
		event.setCanceled(true);
	}

	public V2InfoScreen(String text) {
		this.text = text;
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void initGui() {
		super.initGui();

		textList = new TextList(mc, width, height, 64, height - 42, fontRendererObj);
		textList.addEntries(text);
		textList.addEntry("");

		buttonList.clear();
		buttonList.add(new GuiButton(0, width / 2 + 4 + 75, height - 28, 150, 20, "Zum Discord"));
	}

	public void closeGui() {
		MinecraftForge.EVENT_BUS.unregister(this);
		mc.displayGuiScreen(previousScreen);
	}

	private boolean isLeftButtonHovered(int mouseX, int mouseY) {
		return mouseX > this.width / 2 - 205 && mouseX < this.width / 2 - 120 && mouseY > this.height - 28 && mouseY < this.height - 8;
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawBackground(0);
		textList.drawScreen(mouseX, mouseY, partialTicks);

		String text = "§nGrieferUtils v2 Open Beta";

		int color = Color.HSBtoRGB(System.currentTimeMillis() % 2500 / 2500f, 0.5f, 1);

		// Title
		GlStateManager.scale(1.5, 1.5, 1.5);
		drawCenteredString(fontRendererObj, text, width / 3, 15, color);
		GlStateManager.scale(1/1.5, 1/1.5, 1/1.5);

		// Icon
		GlStateManager.pushMatrix();
		GlStateManager.color(1, 1, 1);
		int textWidth = fontRendererObj.getStringWidth(text);
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/icon.png"));
		LabyMod.getInstance().getDrawUtils().drawRawTexture(width / 2d - textWidth * 0.75 - 29, 18, 256, 256, 20, 20);
		GlStateManager.popMatrix();

		// Left button
		text = isLeftButtonHovered(mouseX, mouseY) ? "§fSchließen" : "§7Schließen";
		LabyMod.getInstance().getDrawUtils().drawString(text, width / 2d - 186, height - 22);

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode != 1)
			super.keyTyped(typedChar, keyCode);
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (isLeftButtonHovered(mouseX, mouseY)) {
			closeGui();
			return;
		}

		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		textList.handleMouseInput();
	}

	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		textList.mouseReleased(mouseX, mouseY, state);
	}

	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		ChatMenuEntry.openWebsite("https://grieferutils.l3g7.dev");
	}

	public static class V2ChangelogSetting extends SmallButtonSetting {

		public V2ChangelogSetting() {
			name("§h§i");
			callback(V2InfoScreen::open);
		}

		@Override
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
			super.draw(x, y, maxX, maxY, mouseX, mouseY);

			int color = Color.HSBtoRGB(System.currentTimeMillis() % 2500 / 2500f, 0.5f, 1);
			Minecraft.getMinecraft().fontRendererObj.drawString(" §lv2.0-BETA", x + 2, y + 7, color, true);
		}

	}

}
