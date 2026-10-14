/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.reflection;

import com.sun.management.HotSpotDiagnosticMXBean;
import dev.l3g7.griefer_utils.core.api.util.Util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;

/**
 * Java 27-compatible unrestricted lookup.
 */
public class Access {

	private static final Lookup lookup;

	static {
		lookup = Util.tryFatal(() -> {
			Lookup lookup = MethodHandles.lookup();

			Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
			Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);

			int allowedModesOffset = (useCompactObjectHeaders() ? 8 : 12); // Result of Unsafe#objectFieldOffset(Lookup#allowedModes)
			unsafeClass.getDeclaredMethod("putInt", Object.class, long.class, int.class)
					.invoke(theUnsafe.get(null), lookup, allowedModesOffset, -1 /* TRUSTED */);
			return lookup;
		});
	}

	private static boolean useCompactObjectHeaders() {
        try {
            HotSpotDiagnosticMXBean bean = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
            return Boolean.parseBoolean(bean.getVMOption("UseCompactObjectHeaders").getValue());
        } catch (IllegalArgumentException e) {
            return false; // Flag is unknown (older than JDK 24)
        }
    }

	public static Lookup getElevatedLookup() {
		return lookup;
	}

}
