package dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.util;

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
