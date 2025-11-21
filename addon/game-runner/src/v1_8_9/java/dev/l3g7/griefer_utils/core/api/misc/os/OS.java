/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.os;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.Bridged;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;

import java.io.File;
import java.util.function.Consumer;

@Bridged
public interface OS {

	OS OS = FileProvider.getBridge(OS.class);

	boolean isFallback();

	void maximizeWindow();

	void chooseFile(Consumer<File> callback, String filterName, String... allowedFileTypes);

}
