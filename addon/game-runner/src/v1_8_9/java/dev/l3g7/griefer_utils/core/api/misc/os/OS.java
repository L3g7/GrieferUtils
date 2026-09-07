/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.os;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.Bridged;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.Display;

import java.io.File;

@Bridged
public interface OS {

	OS OS = FileProvider.getBridge(OS.class);

	default void maximizeWindow() {
		TickScheduler.sync(() -> GLFW.glfwMaximizeWindow(Display.getWindowHandle()));
	}

	@Nullable File chooseImageFile();

}
