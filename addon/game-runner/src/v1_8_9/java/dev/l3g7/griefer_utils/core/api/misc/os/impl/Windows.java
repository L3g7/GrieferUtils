/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.os.impl;

import com.sun.jna.*;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.win32.StdCallLibrary;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.os.OS;
import dev.l3g7.griefer_utils.core.api.misc.os.impl.Windows.Comdlg32.OpenFileName;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.WINDOWS;

@Bridge
@Singleton
@ExclusiveTo(WINDOWS)
public class Windows implements OS {

	private static final Comdlg32 COMDLG32 = Native.load("comdlg32", Comdlg32.class);

	@Override
	public @Nullable File chooseImageFile() {
		String[] allowedFileTypes = ImageIO.getReaderFileSuffixes();
		String filterStr = Arrays.stream(allowedFileTypes).map(f -> "*." + f).collect(Collectors.joining(";"));

		OpenFileName params = new OpenFileName();
		params.lpstrFile = new Memory(1041);
		params.lpstrFile.clear(1041);
		params.nMaxFile = 260;
		params.lpstrFilter = new WString("Bild\0" + filterStr + "\0\0");

		if (COMDLG32.GetOpenFileNameW(params)) {
			String path;
			if (LABY_4.isActive())
				path = params.lpstrFile.getWideString(0);
			else
				path = Reflection.invoke(params.lpstrFile, "getString", 0L, true);
			return new File(path);
		}

		int error = COMDLG32.CommDlgExtendedError();
		if (error != 0) // Selection was aborted by the user
			System.err.println("GetOpenFileName failed with error " + error);

		return null;
	}

	public interface Comdlg32 extends StdCallLibrary {

		boolean GetOpenFileNameW(OpenFileName params);

		int CommDlgExtendedError();

		@SuppressWarnings("unused")
		@FieldOrder({
			"lStructSize", "hwndOwner", "hInstance", "lpstrFilter", "lpstrCustomFilter", "nMaxCustFilter",
			"nFilterIndex", "lpstrFile", "nMaxFile", "lpstrDialogTitle", "nMaxDialogTitle", "lpstrInitialDir",
			"lpstrTitle", "Flags", "nFileOffset", "nFileExtension", "lpstrDefExt", "lCustData", "lpfnHook",
			"lpTemplateName"
		})
		class OpenFileName extends Structure {

			public OpenFileName() {
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

		}

	}

}