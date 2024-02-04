package dev.l3g7.griefer_utils.v1_8_9.misc.gui.elements;

import com.sun.jna.*;
import dev.l3g7.griefer_utils.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.v1_8_9.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.v1_8_9.misc.gui.elements.laby_polyfills.ModTextField;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

public class ImageSelection extends ModTextField implements Drawable, Clickable {

	public static final ResourceLocation MISC_HEAD_QUESTION = new ResourceLocation("griefer_utils", "icons/labymod_3/question.png");

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
		DrawUtils.bindTexture(selection == null ? MISC_HEAD_QUESTION : new ResourceLocation("griefer_utils/user_content/" + selection.hashCode()));
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

	public static class FileSelectionDialog {

		static {
			if (Platform.isWindows())
				Native.register("comdlg32");
		}

		public static native boolean GetOpenFileNameW(OpenFileName params);

		public static native int CommDlgExtendedError();

		public static void chooseFile(Consumer<File> fileConsumer, String filterName, String... allowedFileTypes) {
			new Thread(() -> {
				if (Platform.isWindows())
					windowsFileChooser(fileConsumer, filterName, allowedFileTypes);
				else
					swingFileChooser(fileConsumer, filterName, allowedFileTypes);
			}).start();

		}

		private static void swingFileChooser(Consumer<File> fileConsumer, String filterName, String[] allowedFileTypes) {
			JFileChooser fc = new JFileChooser((File) null);
			fc.setMultiSelectionEnabled(false);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

			if (filterName != null) {
				fc.addChoosableFileFilter(new FileFilter() {
					@Override
					public boolean accept(File f) {
						if (f.isDirectory())
							return true;

						for (String allowedFileType : allowedFileTypes)
							if (f.getName().endsWith("." + allowedFileType))
								return true;

						return false;
					}

					@Override
					public String getDescription() {
						return filterName;
					}
				});
			}

			fileConsumer.accept(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION ? fc.getSelectedFile() : null);
		}

		private static void windowsFileChooser(Consumer<File> fileConsumer, String filterName, String[] allowedFileTypes) {
			OpenFileName params = new OpenFileName();
			params.lpstrFile = new Memory(1041);
			params.lpstrFile.clear(1041);
			params.nMaxFile = 260;
			if (filterName != null)
				params.lpstrFilter = new WString(filterName + "\0" + Arrays.stream(allowedFileTypes).map(f -> "*." + f).collect(Collectors.joining(";")) + "\0\0");


			if (GetOpenFileNameW(params)) {
				fileConsumer.accept(new File(params.lpstrFile.getWideString(0)));
				return;
			}

			int error = CommDlgExtendedError();
			if (error != 0) // Selection was aborted by the user
				System.err.println("GetOpenFileName failed with error " + error);

			fileConsumer.accept(null);
		}
	}

	@SuppressWarnings("unused")
	public static class OpenFileName extends Structure {

		public OpenFileName() {
			super();
			lStructSize = size();
		}

		public int lStructSize;
		public Pointer hwndOwner;
		public Pointer hInstance;
		public WString lpstrFilter;
		public WString lpstrCustomFilter;
		public int nMaxCustFilter;
		public int nFilterIndex;
		public Pointer lpstrFile;
		public int nMaxFile;
		public String lpstrDialogTitle;
		public int nMaxDialogTitle;
		public WString lpstrInitialDir;
		public WString lpstrTitle;
		public int Flags;
		public short nFileOffset;
		public short nFileExtension;
		public String lpstrDefExt;
		public Pointer lCustData;
		public Pointer lpfnHook;
		public Pointer lpTemplateName;

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("lStructSize",
				"hwndOwner", "hInstance", "lpstrFilter", "lpstrCustomFilter"
				, "nMaxCustFilter", "nFilterIndex", "lpstrFile", "nMaxFile"
				, "lpstrDialogTitle", "nMaxDialogTitle", "lpstrInitialDir", "lpstrTitle"
				, "Flags", "nFileOffset", "nFileExtension", "lpstrDefExt"
				, "lCustData", "lpfnHook", "lpTemplateName");
		}
	}

}