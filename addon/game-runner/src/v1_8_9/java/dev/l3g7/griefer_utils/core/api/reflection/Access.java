package dev.l3g7.griefer_utils.core.api.reflection;

import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.BiConsumer;
import dev.l3g7.griefer_utils.core.api.util.Util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
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

			unsafeClass.getDeclaredMethod("putInt", Object.class, long.class, int.class)
					.invoke(theUnsafe.get(null), lookup, 12 /* allowedModes */, -1 /* TRUSTED */);
			return lookup;
		});
	}

	public static Lookup getElevatedLookup() {
		return lookup;
	}

	public static <O, V> BiConsumer<O, V> createSetter(Field field) {
		return Util.tryFatal(() -> {
			MethodHandle setter = lookup.unreflectSetter(field);
			return setter::invoke;
		});
	}

}
