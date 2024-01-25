/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.mapping;

public enum Mapping {

	/**
	 * The original, obfuscated names.
	 */
	OBFUSCATED,

	/**
	 * The intermediary names.
	 * They stay consistent for every mapping version targeting the same minecraft version.
	 */
	SEARGE,

	/**
	 * The names, deobfuscated using MCP mappings.
	 * If no MCP mapping exists for a specific member, the intermediary name will be used.
	 */
	UNOBFUSCATED;

}