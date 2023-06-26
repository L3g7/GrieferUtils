/*
 * This file is part of GrieferUtils https://github.com/L3g7/GrieferUtils.
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 the "License";
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

package dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static dev.l3g7.griefer_utils.core.util.Util.elevate;

public class Util {

	public static Map<String, String> strMap(String... data) {
		Map<String, String> map = new HashMap<>();

		for (int i = 0; i < data.length; i += 2)
			map.put(data[i], data[i + 1]);

		return map;
	}

	public static String urlEncode(Map<String, String> params) {
		StringBuilder builder = new StringBuilder();
		params.forEach((key, value) -> {
			try {
				if (builder.length() != 0)
					builder.append("&");

				builder.append(URLEncoder.encode(key, "UTF-8"));

				if (value != null) {
					builder.append("=");
					builder.append(URLEncoder.encode(value, "UTF-8"));
				}
			} catch (UnsupportedEncodingException e) {
				throw elevate(e);
			}
		});
		return builder.toString();
	}

}
