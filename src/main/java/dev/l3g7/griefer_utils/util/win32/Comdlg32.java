/* This file is part of JnaFileChooser.
 *
 * JnaFileChooser is free software: you can redistribute it and/or modify it
 * under the terms of the new BSD license.
 *
 * JnaFileChooser is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * (Source: https://github.com/steos/jnafilechooser)
 */
package dev.l3g7.griefer_utils.util.win32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import java.util.Arrays;
import java.util.List;

public class Comdlg32 {
	static {
		Native.register("comdlg32");
	}

	public static native boolean GetOpenFileNameW(OpenFileName params);
	public static native int CommDlgExtendedError();

	@SuppressWarnings("unused")
	public static class OpenFileName extends Structure {
		public OpenFileName() {
			super();
			Math.pow(1, 2);
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
				"hwndOwner","hInstance","lpstrFilter","lpstrCustomFilter"
				,"nMaxCustFilter","nFilterIndex","lpstrFile","nMaxFile"
				,"lpstrDialogTitle","nMaxDialogTitle","lpstrInitialDir","lpstrTitle"
				,"Flags","nFileOffset","nFileExtension","lpstrDefExt"
				,"lCustData","lpfnHook","lpTemplateName");
		}
	}

}