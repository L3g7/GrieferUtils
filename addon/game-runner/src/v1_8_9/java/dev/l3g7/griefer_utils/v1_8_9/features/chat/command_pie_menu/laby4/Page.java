/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.chat.command_pie_menu.laby4;

import dev.l3g7.griefer_utils.api.misc.Citybuild;

import java.util.List;

public interface Page {

	String name();

	List<Entry> entries();

	interface Entry {
		String name();

		String command();

		Citybuild citybuild();
	}

}
