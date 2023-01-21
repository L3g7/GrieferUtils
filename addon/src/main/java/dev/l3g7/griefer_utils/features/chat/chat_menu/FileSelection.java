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

package dev.l3g7.griefer_utils.features.chat.chat_menu;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Platform;

import javax.swing.*;
import java.io.File;
import java.util.function.Consumer;

/**
 * description missing.
 */
public class FileSelection {

	static {
		Native.register("comdlg32");
	}

	public static native boolean GetOpenFileNameW(OpenFileName params);
	public static native int CommDlgExtendedError();

	public static void chooseFile(Consumer<File> fileConsumer) {
		/*
		 * Swing file chooser
		 */
		if (!Platform.isWindows()) {
			new Thread(() -> {
				final JFileChooser fc = new JFileChooser((File) null);
				fc.setMultiSelectionEnabled(false);
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

				fileConsumer.accept(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION ? fc.getSelectedFile() : null);
			}).start();
		}

		/*
		 * Windows file chooser
		 */
		OpenFileName params = new OpenFileName();
		params.lpstrFile = new Memory(1041);
		params.lpstrFile.clear(1041);
		params.nMaxFile = 260;

		if (GetOpenFileNameW(params)) {
			fileConsumer.accept(new File(params.lpstrFile.getString(0, true)));
			return;
		}

		int error = CommDlgExtendedError();
		if (error != 0) // Selection was aborted by the user
			System.err.println("GetOpenFileName failed with error " + error);

		fileConsumer.accept(null);
	}

}
