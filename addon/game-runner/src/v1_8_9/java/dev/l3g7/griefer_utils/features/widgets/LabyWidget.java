/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

public abstract class LabyWidget {

	private Object versionedWidget = null;

	protected abstract Object getLaby3();

	protected abstract Object getLaby4();

	public <T> T getVersionedWidget() {
		if (versionedWidget == null)
			versionedWidget = LABY_3.isActive() ? getLaby3() : getLaby4();

		return c(versionedWidget);
	}

}
