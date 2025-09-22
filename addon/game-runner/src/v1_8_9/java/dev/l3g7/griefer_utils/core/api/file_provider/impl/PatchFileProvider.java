package dev.l3g7.griefer_utils.core.api.file_provider.impl;

import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.core.api.util.IOUtil;
import dev.l3g7.griefer_utils.core.api.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.misc.UnsafeJsonSerializer.UNSAFE;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

public class PatchFileProvider extends FileProvider {

	public static final PatchFileProvider INSTANCE = new PatchFileProvider();
	private static final File PATCH_DIR = new File(new File("GrieferUtils"), "patches");
	private static final PatchLoader LOADER = new PatchLoader();

	private PatchFileProvider() {}

	@Override
	protected Throwable update0(Class<?> refClass) {
		if (!PATCH_DIR.exists() || !PATCH_DIR.isDirectory())
			return null;

		for (File file : PATCH_DIR.listFiles((file, name) -> name.endsWith(".class"))) {
			try {
				Class<?> clazz = LOADER.defineClass(IOUtil.toByteArray(new FileInputStream(file)));
				LabyBridge.run(() -> {}, () -> {
					UNSAFE.putObject(clazz, 48, getClass().getModule());
					UNSAFE.putObject(clazz.getPackage(), 16, getClass().getModule());
				});

				String path = clazz.getName().replace('.', '/') + ".class";
				fileCache.put(path, () -> new FileInputStream(file));
				classMetaCache.put(path, new ClassMeta(clazz));
			} catch (Throwable e) {
				System.err.println("Error while loading patch " + file.getAbsolutePath());
				e.printStackTrace();
				return e;
			}
		}

		return null;
	}

	private static class PatchLoader extends URLClassLoader {

		public PatchLoader() {
			super(new URL[0], PatchFileProvider.class.getClassLoader());
			if (LABY_3.isActive()) {
				try {
					// Copy packages
					Field field = ClassLoader.class.getDeclaredField("packages");
					field.setAccessible(true);

					HashMap<String, Package> src = c(field.get(PatchFileProvider.class.getClassLoader()));
					HashMap<String, Package> current = c(field.get(getClass().getClassLoader()));
					current.putAll(src);
				} catch (ReflectiveOperationException e) {
					throw Util.elevate(e);
				}
			}
		}

		public Class<?> defineClass(byte[] bytes) {
			return defineClass(null, bytes, 0, bytes.length);
		}

	}

}
