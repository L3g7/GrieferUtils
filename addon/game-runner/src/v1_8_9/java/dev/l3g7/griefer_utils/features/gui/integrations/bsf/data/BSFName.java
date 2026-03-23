/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.gui.integrations.bsf.data;

public record BSFName(String pronomialSuffix, String singular, String plural) {

	public BSFName(String pronomialSuffix, String singular, String plural) {
		this.pronomialSuffix = pronomialSuffix;
		this.singular = singular;
		this.plural = plural.length() < singular.length() ? singular + plural : plural; // Allow either suffix (Mine, n) or completely new words (Dorf, Dörfer)
	}

}
