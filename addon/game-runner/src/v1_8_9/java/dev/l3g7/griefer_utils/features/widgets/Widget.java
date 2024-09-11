/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets;

import dev.l3g7.griefer_utils.features.widgets.Laby3Widget.ComplexLaby3Widget;
import dev.l3g7.griefer_utils.features.widgets.Laby4Widget.ComplexLaby4Widget;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

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

	public abstract static class SimpleWidget extends ComplexWidget {

		public final String name;

		public SimpleWidget() {
			this.name = null;
		}

		public SimpleWidget(String name) {
			this.name = name;
		}

		public abstract String getValue();

		@Override
		public KVPair[] getLines() {
			return new KVPair[] {
				new KVPair(name == null ? null : new ChatComponentText(name), getValue(), getColor())
			};
		}

		public int getColor() {
			return -1;
		}

	}

	public abstract static class ComplexWidget extends Widget {

		public abstract KVPair[] getLines();

		public boolean isVisibleInGame() {
			return true;
		}

		@Override
		protected LabyWidget getLaby3() {
			return new ComplexLaby3Widget(this);
		}

		@Override
		protected LabyWidget getLaby4() {
			return new ComplexLaby4Widget(this);
		}

		public static class KVPair {
			public final IChatComponent key;
			public final Object value;
			public final int color;

			public KVPair(IChatComponent key, IChatComponent value) {
				this.key = key;
				this.value = value;
				this.color = -1;
			}

			private KVPair(IChatComponent key, String value, int color) {
				this.key = key;
				this.value = value;
				this.color = color;
			}

		}
	}

	public interface LabyWidget {
		void setOwner(Widget widget);
	}

}
