package dev.l3g7.griefer_utils.settings.elements.filesetting;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.elements.ButtonSetting;
import dev.l3g7.griefer_utils.settings.elements.SmallButtonSetting;
import dev.l3g7.griefer_utils.util.IOUtil;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.gui.elements.Scrollbar;
import net.labymod.settings.elements.SettingsElement;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileSetting extends SmallButtonSetting {

	protected final List<File> files = new ArrayList<>();

	public FileSetting(String header) {
		subSettingsWithHeader(header, new ButtonSetting()
			.name("Datei hinzufügen")
			.callback(() -> IOUtil.chooseFile(file -> {
				if (file == null)
					return;

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

	public List<File> getFiles() {
		return files;
	}

	public void clearFiles() {
		files.clear();
		getSubSettings().getElements().removeIf(s -> s instanceof FileSetting);
	}
}
