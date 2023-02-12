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

import com.sun.jna.Platform;
import dev.l3g7.griefer_utils.features.uncategorized.settings.debug.log.log_entries.*;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.SmallButtonSetting;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.misc.crypto.AESCipher;
import dev.l3g7.griefer_utils.core.misc.crypto.RSACipher;
import net.labymod.settings.elements.ControlElement;
import net.minecraft.client.Minecraft;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.ZipOutputStream;

public class Log {

	private static final String showInExplorerCommand;
	private static final File file = new File(new File(Minecraft.getMinecraft().mcDataDir, "logs"), "griefer_utils_log_bundle.zip");

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
		.callback(Log::save);

	public static final CategorySetting category = new CategorySetting()
		.name("Log")
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
			OutputStream fileOut = Files.newOutputStream(file.toPath());

			bundle:
			if (encrypt.get() && RSACipher.isAvailable()) {
				AESCipher aesCipher = AESCipher.generateNew();
				if (!aesCipher.isAvailable()) {
					bundle(fileOut);
					break bundle;
				}

				ByteArrayOutputStream content = new ByteArrayOutputStream();
				bundle(content);
				fileOut.write(RSACipher.encode(aesCipher.getKeyIvBundle()));
				fileOut.write(aesCipher.encode(content.toByteArray()));
			} else
				bundle(fileOut);

			fileOut.close();

			// Open in explorer
			if (showInExplorerCommand == null) {
				MinecraftUtil.displayAchievement("Log wurde gespeichert.", "Pfad: " + file.getAbsolutePath());
				return;
			}

			try {
				Runtime.getRuntime().exec(showInExplorerCommand + (Platform.isLinux() ? file.getParentFile() : file).getAbsolutePath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

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