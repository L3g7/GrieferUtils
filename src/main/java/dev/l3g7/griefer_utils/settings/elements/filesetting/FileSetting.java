package dev.l3g7.griefer_utils.settings.elements.filesetting;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.elements.ButtonSetting;
import dev.l3g7.griefer_utils.settings.elements.SmallButtonSetting;
import dev.l3g7.griefer_utils.util.IOUtil;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.gui.elements.Scrollbar;
import net.labymod.settings.elements.SettingsElement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.features.Feature.displayAchievement;

public class FileSetting extends SmallButtonSetting {

	protected final List<File> files = new ArrayList<>();
	private Long totalLimit = null;
	private String limitString = null;

	public FileSetting(String header) {
		subSettingsWithHeader(header, new ButtonSetting()
			.name("Datei hinzufügen")
			.callback(() -> IOUtil.chooseFile(file -> {
				if (file == null)
					return;

				// Check the total file size
				if (totalLimit != null) {
					try {
						long size = 0;
						for (File f : files)
							size += Files.size(f.toPath());
						size += Files.size(file.toPath());

						if (totalLimit < size) {
							displayAchievement("§e§l§nDateien zu groß", "§eAlle Datein zusammen dürfen maximal %s groß sein.", limitString);
							return;
						} else System.out.println(size);
					} catch (IOException ignored) {
						displayAchievement("§c§l§nFehler \u26A0", "§cEs ist Fehler aufgetreten.\n(Wurde ein Anhang gelöscht?)");
						return;
					}
				}
				files.add(file);
				List<SettingsElement> settings = getSubSettings().getElements();
				settings.add(settings.size() - 1, new FileEntry(file, this));
				mc.currentScreen.initGui();
			}))
		);

		callback(() -> {
			Feature.path().add(this);

			Scrollbar scrollbar = Reflection.get(mc.currentScreen, "scrollbar");
			Reflection.set(mc.currentScreen, scrollbar.getScrollY(), "preScrollPos");
			scrollbar.setScrollY(0);
			mc.currentScreen.initGui();
		});

		buttonIcon(new IconData("griefer_utils/icons/file.png"));
	}

	/**
	 * Sets the maximum file size of all files combined
	 * @param totalLimit the maximum size, in bytes.
	 * @param limitString a representation of the maximum size as a string, used for the error toast.
	 */
	public FileSetting totalLimit(Long totalLimit, String limitString) {
		this.totalLimit = totalLimit;
		this.limitString = limitString;
		return this;
	}

	public List<File> getFiles() {
		return files;
	}

	public void clearFiles() {
		files.clear();
		getSubSettings().getElements().removeIf(s -> s instanceof FileSetting);
	}

}
