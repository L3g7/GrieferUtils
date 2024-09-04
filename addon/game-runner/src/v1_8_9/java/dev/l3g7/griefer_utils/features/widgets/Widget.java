/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets;

import dev.l3g7.griefer_utils.features.widgets.Laby3Widget.SimpleLaby3Widget;
import dev.l3g7.griefer_utils.features.widgets.Laby4Widget.SimpleLaby4Widget;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

public abstract class Widget {

	private LabyWidget versionedWidget = null;

	protected LabyWidget getLaby3() {
		throw new UnsupportedOperationException();
	}

	protected LabyWidget getLaby4() {
		throw new UnsupportedOperationException();
	}

	public <T> T getVersionedWidget() {
		if (versionedWidget == null) {
			versionedWidget = LABY_3.isActive() ? getLaby3() : getLaby4();
			versionedWidget.setOwner(this);
		}

		return c(versionedWidget);
	}

	public abstract static class SimpleWidget extends Widget {

		public final String name;
		public final String defaultValue;

		public SimpleWidget() {
			this(null, "");
		}

		public SimpleWidget(String defaultValue) {
			this(null, defaultValue);
		}

		public SimpleWidget(String name, String defaultValue) {
			this.name = name;
			this.defaultValue = defaultValue;
		}

		public abstract String getValue();

		public int getColor() {
			return -1;
		}

		public boolean isVisibleInGame() {
			return true;
		}

		@Override
		protected LabyWidget getLaby3() {
			return new SimpleLaby3Widget(this);
		}

		@Override
		protected LabyWidget getLaby4() {
			return new SimpleLaby4Widget(this);
		}
	}

	public interface LabyWidget {
		void setOwner(Widget widget);
	}

}
