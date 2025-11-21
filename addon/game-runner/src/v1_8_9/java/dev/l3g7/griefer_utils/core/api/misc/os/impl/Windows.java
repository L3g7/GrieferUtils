/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.os.impl;

import com.sun.jna.*;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.os.OS;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.opengl.Display;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.WINDOWS;

@Bridge
@Singleton
@ExclusiveTo(WINDOWS)
public class Windows implements OS {

	@Override
	public boolean isFallback() {
		return false;
	}

	@Override
	public void maximizeWindow() {
		long handle = LABY_4.isActive()
			? GLFWNativeWin32.glfwGetWin32Window(Display.getWindowHandle())
			: Reflection.invoke(Reflection.invoke(Display.class, "getImplementation"), "getHwnd");

		HWND hwnd = new HWND(new Pointer(handle));
		User32.ShowWindow(hwnd, WinUser.SW_SHOWMAXIMIZED);
		User32.SetForegroundWindow(hwnd);
		User32.SetActiveWindow(hwnd);
	}

	@Override
	public void chooseFile(Consumer<File> callback, String filterName, String... allowedFileTypes) {
		Comdlg32.OpenFileName params = new Comdlg32.OpenFileName();
		params.lpstrFile = new Memory(1041);
		params.lpstrFile.clear(1041);
		params.nMaxFile = 260;
		if (filterName != null)
			params.lpstrFilter = new WString(filterName + "\0" + Arrays.stream(allowedFileTypes).map(f -> "*." + f).collect(Collectors.joining(";")) + "\0\0");

		if (Comdlg32.GetOpenFileNameW(params)) {
			String path;
			if (LABY_4.isActive())
				path = params.lpstrFile.getWideString(0);
			else
				path = Reflection.invoke(params.lpstrFile, "getString", 0L, true);
			callback.accept(new File(path));
			return;
		}

		int error = Comdlg32.CommDlgExtendedError();
		if (error != 0) // Selection was aborted by the user
			System.err.println("GetOpenFileName failed with error " + error);

		callback.accept(null);
	}

	private static class User32 {

		static {
			Native.register("user32");
		}

		static native HWND SetActiveWindow(HWND hwnd);

		static native boolean SetForegroundWindow(HWND hwnd);

		static native boolean ShowWindow(HWND hwnd, int nCmdShow);

	}

	private static class Comdlg32 {

		static {
			Native.register("comdlg32");
		}

		static native boolean GetOpenFileNameW(OpenFileName params);

		static native int CommDlgExtendedError();

		@SuppressWarnings("unused")
		static class OpenFileName extends Structure {

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

}
