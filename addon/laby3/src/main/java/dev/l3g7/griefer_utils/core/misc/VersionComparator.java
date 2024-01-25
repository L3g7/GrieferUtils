/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionComparator implements Comparator<String> {

	private final Pattern VERSION_PATTERN = Pattern.compile("^v?(?<version>(?:\\d+\\.)*\\d+)(?:-(?<meta>[\\w.+-]+?)(?<metaid>[\\d.]*))?$");

	@Override
	public int compare(String o1, String o2) {
		Matcher m1 = VERSION_PATTERN.matcher(o1);
		if (!m1.find())
			return 0;

		Matcher m2 = VERSION_PATTERN.matcher(o2);
		if (!m2.find())
			return 0;

		String[] m1Parts = m1.group("version").split("\\.");
		String[] m2Parts = m2.group("version").split("\\.");

		// Compare version
		int i;
		for (i = 0; i < m1Parts.length; i++) {
			if (i == m2Parts.length)
				return -1;

			int c = Integer.compare(Integer.parseInt(m1Parts[i]), Integer.parseInt(m2Parts[i]));
			if (c != 0)
				return -c;
		}
		if (i != m2Parts.length)
			return 1;

		// Compare meta tag
		if (m1.group("meta") == null)
			return m2.group("meta") == null ? 0 : -1;
		else if (m2.group("meta") == null)
			return 1;

		int metaCmp = -m1.group("meta").compareToIgnoreCase(m2.group("meta"));
		if (metaCmp != 0)
			return metaCmp;

		// Compare meta id
		if (m1.group("metaid").isEmpty()) {
			if (m2.group("metaid").isEmpty())
				return 0;
			return 1;
		}
		if (m2.group("metaid").isEmpty()) {
			return -1;
		}

		return compare(m1.group("metaid"), m2.group("metaid"));
	}

}