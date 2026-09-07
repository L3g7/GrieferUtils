/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.os;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.Fallback;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

@Bridge
@Fallback
@Singleton
public class OSFallback implements OS {

	@Override
	public @Nullable File chooseImageFile() {
		// Open javax.swing file chooser
		JFileChooser fc = new JFileChooser((File) null);
		fc.setMultiSelectionEnabled(false);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

		String[] allowedFileTypes = ImageIO.getReaderFileSuffixes();
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
				return "Bild";
			}
		});

		return fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION ? fc.getSelectedFile() : null;
	}
}
