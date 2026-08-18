/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.gui.elements;

import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.misc.ThreadFactory;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.ModTextField;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import static dev.l3g7.griefer_utils.core.api.misc.os.OS.OS;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static java.lang.Thread.MIN_PRIORITY;

public class ImageSelection extends ModTextField implements Drawable, Clickable {

	public static final ResourceLocation FALLBACK = new ResourceLocation("griefer_utils", "icons/tree_file.png");

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
			.callback(() -> chooseFile(this::processChosenFile, "Bild", ImageIO.getReaderFileSuffixes()));

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
				ResourceLocation location = new ResourceLocation("griefer_utils", "icons/user_content/" + file.hashCode() + ".png");
				mc().getTextureManager().loadTexture(location, new DynamicTexture(img));
			});
		} catch (IOException | NullPointerException e) {
			LabyBridge.labyBridge.notifyError("Die Datei konnte nicht als Bild geladen werden.");
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
		DrawUtils.drawString(label, xPosition, y, 1.2);

		// Draw preview of file
		DrawUtils.bindTexture(selection == null ? FALLBACK : new ResourceLocation("griefer_utils", "icons/user_content/" + selection.hashCode() + ".png"));
		DrawUtils.drawTexture(x, yPosition, 256.0, 256.0, 20, 20);

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

	private static void chooseFile(Consumer<File> fileConsumer, String filterName, String... allowedFileTypes) {
		ThreadFactory.run("GrieferUtils File Chooser", MIN_PRIORITY,
			() -> OS.chooseFile(fileConsumer, filterName, allowedFileTypes));
	}

}