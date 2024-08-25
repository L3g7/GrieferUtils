/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets;

public abstract class SimpleWidget {

	private final String name;
	private final String defaultValue;
	private String value;

	public SimpleWidget() {
		this(null, "");
	}

	public SimpleWidget(String defaultValue) {
		this(null, defaultValue);
	}

	public SimpleWidget(String name, String defaultValue) {
		this.name = name;
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public abstract String getValue();

	public int getColor() {
		return -1;
	}

	public boolean isVisibleInGame() {
		return true;
	}

}
