package dev.l3g7.griefer_utils.misc.gui.elements;

import dev.l3g7.griefer_utils.core.misc.os.FileSelectionDialog;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.gui.elements.ModTextField;
import net.labymod.utils.DrawUtils;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static net.labymod.main.ModTextures.MISC_HEAD_QUESTION;

public class ImageSelection extends ModTextField implements Drawable, Clickable {

	protected static final DrawUtils drawUtils = MinecraftUtil.drawUtils();
	private static final double LABEL_HEIGHT = 9 * 1.2;

	private final Button button;
	private final String label;
	private double x, y;
	private int renderGroup = 0;

	private File selection = null;

	ImageSelection(String label) {
		super(0, mc().fontRendererObj, 0, 0, 0, 20);
		this.button = new Button("Auswählen")
			.size(60, 20)
			.callback(() -> FileSelectionDialog.chooseFile(this::processChosenFile, "Bild", ImageIO.getReaderFileSuffixes()));

		this.label = label;
		setMaxStringLength(Integer.MAX_VALUE);
		setPlaceHolder("§8Wähle eine Datei aus");
	}

	private void processChosenFile(File file) {
		// Check if user has selected a file
		if (file == null)
			return;

		try {
			// Read image
			BufferedImage img = ImageIO.read(file);

			// Add image to Minecraft's texture manager
			mc().addScheduledTask(() -> {
				ResourceLocation location = new ResourceLocation("griefer_utils/user_content/" + file.hashCode());
				mc().getTextureManager().loadTexture(location, new DynamicTexture(img));
			});
		} catch (IOException | NullPointerException e) {
			MinecraftUtil.displayAchievement("§e§l§nFehlerhafte Datei", "§eDie Datei konnte nicht als Bild geladen werden.");
			return;
		}

		selection = file;
	}

	public ImageSelection x(double x) {
		this.x = x;
		xPosition = (int) x + 26;
		button.x(xPosition + width + 5);
		return this;
	}

	public ImageSelection y(double y) {
		this.y = y;
		yPosition = (int) (y + LABEL_HEIGHT + 5);
		button.y(yPosition);
		return this;
	}

	public ImageSelection pos(double x, double y) {
		return x(x).y(y);
	}

	public ImageSelection width(double width) {
		this.width = (int) width - 26 - button.getButtonWidth() - 5;
		button.x(xPosition + this.width + 5);
		return this;
	}

	public double bottom() {
		return yPosition + height;
	}

	public ImageSelection renderGroup(int renderGroup) {
		this.renderGroup = renderGroup;
		button.renderGroup(renderGroup);
		return this;
	}

	public ImageSelection select(File file) {
		this.selection = file;
		return this;
	}

	public File getSelection() {
		return selection;
	}

	@Override
	public void draw(int mouseX, int mouseY, int renderGroup) {
		if (this.renderGroup != renderGroup)
			return;

		// Draw label
		drawUtils.drawString(label, xPosition, y, 1.2);

		// Draw preview of file
		drawUtils.bindTexture(selection == null ? MISC_HEAD_QUESTION : new ResourceLocation("griefer_utils/user_content/" + selection.hashCode()));
		drawUtils.drawTexture(x, yPosition, 256.0, 256.0, 20, 20);

		// Draw textbox showing name of file
		setText(selection == null ? "" : selection.getName());
		super.drawTextBox();

		// Draw select button
		button.draw(mouseX, mouseY, renderGroup);
	}

	@Override
	public void mousePressed(int mouseX, int mouseY, int mouseButton) {
		button.mousePressed(mouseX, mouseY, mouseButton);
//		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

}