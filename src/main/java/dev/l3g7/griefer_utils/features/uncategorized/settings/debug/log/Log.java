/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.settings.debug.log;

import dev.l3g7.griefer_utils.features.uncategorized.settings.debug.log.log_entries.*;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.SmallButtonSetting;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import dev.l3g7.griefer_utils.util.misc.Constants;
import net.labymod.settings.elements.ControlElement;
import sun.awt.datatransfer.TransferableProxy;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipOutputStream;

public class Log {

	private static final LogEntry modMetadata = new ModAddonMetadataEntry();
	private static final LogEntry modConfig = new ModAddonConfigEntry();
	private static final LogEntry grieferUtilsConfig = new GrieferUtilsConfigEntry();
	private static final LogEntry labyModConfig = new LabyModConfigEntry();
	private static final LogEntry widgetConfig = new WidgetConfigEntry();
	private static final LogEntry minecraftConfig = new MinecraftConfigEntry();

	private static final LogEntry playerMetadata = new PlayerMetadataEntry();
	private static final LogEntry worldMetadata = new WorldMetadataEntry();
	private static final LogEntry clientMetadata = new ClientMetadataEntry();

	private static final LogEntry log = new LogFileEntry();

	private static final BooleanSetting encrypt = new BooleanSetting()
		.name("Log verschlüsseln")
		.config("settings.debug.log.encrypt")
		.defaultValue(true)
		.custom("Ja", "Nein")
		.icon("lock");

	private static final SmallButtonSetting save = new SmallButtonSetting()
		.name("Speichern")
		.icon("white_scroll")
		.buttonIcon(new ControlElement.IconData("labymod/textures/buttons/download.png"))
		.callback(() -> {
			Log.save();
			MinecraftUtil.displayAchievement("Log wurde kopiert.", "");
		});

	public static final CategorySetting category = new CategorySetting()
		.name("Log")
		.description("GrieferUtils sammelt Nutzungsdaten, um die Benutzerfreundlichkeit zu verbessern. Da die Daten uns helfen, würde es uns freuen, wenn sie gesendet werden ^.^", "", "§7§oUm Spam vorzubeugen, wird zusätzlich zu den einstellbaren Daten ein Hash deiner IP gespeichert. Alle erhobenen Daten werden bis zu 365 Tage lang gespeichert. Falls du die erhobenen Daten erhalten oder löschen willst, melde dich bei einem Entwickler über Discord oder schreibe eine Email an grieferutils@l3g7.dev")
		.icon("white_scroll")
		.subSettings(Arrays.asList(
			new HeaderSetting("§r"),
			new HeaderSetting("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
			new HeaderSetting("§e§lDebug - Log").scale(.7),
			new HeaderSetting("§r").scale(.4).entryHeight(10),
			modMetadata.getSetting(),
			modConfig.getSetting(),
			grieferUtilsConfig.getSetting(),
			labyModConfig.getSetting(),
			widgetConfig.getSetting(),
			minecraftConfig.getSetting(),
			new HeaderSetting(),
			playerMetadata.getSetting(),
			worldMetadata.getSetting(),
			clientMetadata.getSetting(),
			new HeaderSetting(),
			log.getSetting(),
			new HeaderSetting(),
			encrypt,
			save
		));

	private static void save() {
		try {
			Path path = Paths.get(System.getProperty("java.io.tmpdir"), "griefer_utils_log_bundle.zip");
			OutputStream fileOut = Files.newOutputStream(path);

			if (encrypt.get()) {
				ByteArrayOutputStream content = new ByteArrayOutputStream();
				bundle(content);
				Encryption.INSTANCE.encrypt(content.toByteArray(), fileOut);
			} else
				bundle(fileOut);

			fileOut.close();

			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new FileTransferable(Collections.singletonList(path.toFile())), null);
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}
	}

	private static void bundle(OutputStream out) throws IOException {
		ZipOutputStream zip = new ZipOutputStream(out);
		zip.setComment("GrieferUtils log bundle");
		zip.setLevel(Deflater.BEST_COMPRESSION);

		for (LogEntry entry : Arrays.asList(modMetadata, modConfig, grieferUtilsConfig, labyModConfig, widgetConfig, minecraftConfig, playerMetadata, worldMetadata, clientMetadata, log)) {
			entry.addEntry(zip);
		}

		zip.close();
	}

	private static class FileTransferable implements Transferable {

		private final List<File> listOfFiles;

		public FileTransferable(List<File> listOfFiles) {
			this.listOfFiles = listOfFiles;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{DataFlavor.javaFileListFlavor};
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.javaFileListFlavor.equals(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) {
			return listOfFiles;
		}
	}

}