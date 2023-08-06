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

package dev.l3g7.griefer_utils.core.misc.os;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.WString;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * description missing.
 */
public class FileSelectionDialog {

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
			fileConsumer.accept(new File(params.lpstrFile.getString(0, true)));
			return;
		}

		int error = CommDlgExtendedError();
		if (error != 0) // Selection was aborted by the user
			System.err.println("GetOpenFileName failed with error " + error);

		fileConsumer.accept(null);
	}
}
