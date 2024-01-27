/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby4;

import dev.l3g7.griefer_utils.api.mapping.Mapper;
import dev.l3g7.griefer_utils.api.misc.LibLoader;

public class EarlyStart {

	public static void start() throws Throwable {

		// Load mcp mappings for automatic name resolution in Reflection
		Mapper.loadMappings("1.8.9", "22");

		// Load and inject libraries
		LibLoader.loadLibraries(

			// mXparser: for evaluating expressions (Calculator)
			"https://repo1.maven.org/maven2",
			"org/mariuszgromada/math", "MathParser.org-mXparser", "5.1.0",
			"B5472B5E1BBEFEA2DA6052C68A509C84C7F2CA5F99B76A4C5F58354C08818630",

			// ZXing: for reading qr codes (QRCodeScanner)
			"https://repo1.maven.org/maven2",
			"com/google/zxing", "core", "3.5.1",
			"1BA7C0FBB6C267E2FB74E1497D855ADAE633CCC98EDC8C75163AA64BC08E3059"
		);
	}

}