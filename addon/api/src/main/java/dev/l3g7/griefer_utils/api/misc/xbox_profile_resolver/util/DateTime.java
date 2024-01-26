/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.api.misc.xbox_profile_resolver.util;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class DateTime {

	private final Instant wrappedDate;

	public static DateTime now() {
		return new DateTime();
	}

	public DateTime() {
		wrappedDate = Instant.now();
	}

	public DateTime(String dateTime) {
		wrappedDate = Instant.parse(dateTime);
	}

	public DateTime(long unixTime) {
		wrappedDate = Instant.ofEpochSecond(unixTime);
	}

	public DateTime add(long value, TimeUnit format) {
		return new DateTime(getUnixTime() + format.toSeconds(value));
	}

	public long getUnixTime() {
		return wrappedDate.getEpochSecond();
	}

	public boolean after(DateTime dateTime) {
		return getUnixTime() > dateTime.getUnixTime();
	}

}
