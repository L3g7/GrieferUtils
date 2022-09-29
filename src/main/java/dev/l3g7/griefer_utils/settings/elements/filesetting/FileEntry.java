package dev.l3g7.griefer_utils.settings.elements.filesetting;

import com.sun.jna.Platform;
import dev.l3g7.griefer_utils.features.Feature;
import net.labymod.main.LabyMod;
import net.labymod.main.ModTextures;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.ModColor;
import net.labymod.utils.manager.TooltipHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.IOException;

public class FileEntry extends ControlElement {

	private static final String showInExplorerCommand;

	private final File file;
	private final FileSetting parent;
	private int hoverButtonId = 0;
	private String fileName;

	public FileEntry(File file, FileSetting parent) {
		super(null, null);

		this.file = file;
		this.parent = parent;

		// Shorten file name if needed
		if (mc.fontRendererObj.getStringWidth(fileName = file.getName()) <= 142)
			return;

		int dotWidth = mc.fontRendererObj.getStringWidth("...");

		do {
			fileName = fileName.substring(0, fileName.length() - 1);
		} while (mc.fontRendererObj.getStringWidth(fileName) > 142 - dotWidth && fileName.length() > 0);


		fileName += "...";
	}

	@Override
	public int getObjectWidth() {
		return 0;
	}

	@Override
	public int getEntryHeight() {
		return 18;
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton != 0)
			return;

		if (hoverButtonId == 0)
			return;

		// Remove
		if (hoverButtonId == 1) {
			parent.getSubSettings().getElements().remove(this);
			parent.files.remove(file);
			mc.currentScreen.initGui();
			return;
		}

		// Open in explorer
		if (showInExplorerCommand == null) {
			Feature.displayAchievement("Unbekanntes OS", "Bitte melde dem GrieferUtils-Team, welches Betriebssystem du benutzt.");
		} else {
			try {
				Runtime.getRuntime().exec(showInExplorerCommand + (Platform.isLinux() ? file.getParentFile() : file).getAbsolutePath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		hoverButtonId = 0;

		LabyMod.getInstance().getDrawUtils().drawString(fileName, x + 9, y + 5);

		int marginY = (maxY - y - 14) / 2;
		if (drawButton(ModTextures.BUTTON_TRASH, y, 10, marginY, maxX, mouseX, mouseY, "Datei entfernen"))
			hoverButtonId = 1;

		if (drawButton(new ResourceLocation("griefer_utils/icons/explorer.png"), y, 30, marginY, maxX, mouseX, mouseY, "Datei im Explorer ansehen"))
			hoverButtonId = 2;
	}

	/**
	 * Copied from AddonElement.drawButton and edited
	 */
	private boolean drawButton(ResourceLocation resourceLocation, int y, int marginX, int marginY, int maxX, int mouseX, int mouseY, String hoverText) {
		DrawUtils draw = LabyMod.getInstance().getDrawUtils();
		boolean hover = mouseX > maxX - 13 - marginX && mouseX < maxX - marginX + 1 && mouseY > y + marginY + 1 && mouseY < y + 15 + marginY;

		marginX += hover ? 1 : 0;
		int colorA = mkColor(hover ? 10 : 220);
		int colorB = mkColor(hover ? 150 : 0);
		int colorC = mkColor(hover ? 150 : 180);

		draw.drawRectangle(maxX - 14 - marginX, y + marginY, maxX - marginX, y + 14 + marginY, colorA);
		draw.drawRectangle(maxX - 13 - marginX, y + marginY + 1, maxX - marginX + 1, y + 15 + marginY, colorB);
		draw.drawRectangle(maxX - 13 - marginX, y + marginY + 1, maxX - marginX, y + 14 + marginY, colorC);

		Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);
		draw.drawTexture(maxX - 11 - marginX + (hover ? 1 : 0), y + marginY + 3 + (hover ? 1 : 0), 256, 256, 8, 8, 0.8f);

		if (hover)
			TooltipHelper.getHelper().pointTooltip(mouseX, mouseY, 0L, hoverText);

		return hover;
	}

	private int mkColor(int color) {
		return ModColor.toRGB(color, color, color, 255);
	}

	static {
		// Determine right command for platform
		if (Platform.isWindows())
			showInExplorerCommand = "explorer /select, ";
		else if (Platform.isLinux())
			showInExplorerCommand = "xdg-open ";
		else if (Platform.isMac())
			showInExplorerCommand = "open -R ";
		else
			showInExplorerCommand = null;
	}

}