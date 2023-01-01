/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.util.misc;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionComparator implements Comparator<String> {

	private final Pattern VERSION_PATTERN = Pattern.compile("v?(?<version>(?:\\d+\\.)*\\d+)(?:-(?<meta>[\\w.+-]+))?");

	@Override
	public int compare(String o1, String o2) {
		Matcher m1 = VERSION_PATTERN.matcher(o1); m1.find();
		Matcher m2 = VERSION_PATTERN.matcher(o2); m2.find();
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

		// Compare meta
		if (m1.group("meta") == null)
			return m2.group("meta") == null ? 0 : -1;
		else if (m2.group("meta") == null)
			return 1;

		return -m1.group("meta").compareToIgnoreCase(m2.group("meta"));
	}

}