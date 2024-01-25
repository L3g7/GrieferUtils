/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.os;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class OpenFileName extends Structure {

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
