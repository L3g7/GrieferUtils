package dev.l3g7.griefer_utils.core.api.file_provider.impl;

import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.core.api.util.IOUtil;
import dev.l3g7.griefer_utils.core.api.util.Util;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.misc.UnsafeJsonSerializer.UNSAFE;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;
import static org.objectweb.asm.ClassReader.*;

public class PatchFileProvider extends FileProvider {

	public static final PatchFileProvider INSTANCE = new PatchFileProvider();
	private static final File PATCH_DIR = new File(new File("GrieferUtils"), "patches");
	private static final PatchLoader LOADER = new PatchLoader();

	private static final Map<String, ClassMeta> PATCHES = new HashMap<>();

	private static final long CLASS_MODULE_OFFSET = 48;
	private static final long PACKAGE_MODULE_OFFSET = 16;

	private PatchFileProvider() {}

	@Override
	protected Throwable update0(Class<?> refClass) {
		if (!PATCH_DIR.exists() || !PATCH_DIR.isDirectory())
			return null;

		Method runTransformers;
		try {
			runTransformers = Launch.classLoader.getClass().getDeclaredMethod("runTransformers", String.class, String.class, byte[].class);
			runTransformers.setAccessible(true);
		} catch (ReflectiveOperationException e) {
			return e;
		}

		int loadedPatches = 0;

		for (File file : PATCH_DIR.listFiles((file, name) -> name.endsWith(".class"))) {
			try {
				String name = "dev/l3g7/griefer_utils/path/" + file.getName();
				byte[] bytes = IOUtil.toByteArray(new FileInputStream(file));
				bytes[7 /* major_version */] = 52 /* Java 1.8 */;

				ClassNode cn = new ClassNode();
				ClassReader cr = new ClassReader(bytes);
				cr.accept(cn, SKIP_CODE | SKIP_DEBUG | SKIP_FRAMES);
				String path = cn.name + ".class";

				fileCache.put(path, () -> new FileInputStream(file));
				PATCHES.put(path, new ClassMeta(cn, () -> {
					byte[] transformedBytes = (byte[]) runTransformers.invoke(Launch.classLoader, cn.name, cn.name, bytes);
					Class<?> clazz = LOADER.defineClass(transformedBytes);
					LabyBridge.run(() -> {}, () -> {
						// Spoof patch's module to current one
						// (Otherwise the package info is lost)
						UNSAFE.putObject(clazz, CLASS_MODULE_OFFSET, getClass().getModule());
						UNSAFE.putObject(clazz.getPackage(), PACKAGE_MODULE_OFFSET, getClass().getModule());
					});
					return clazz;
				}));
				loadedPatches++;
			} catch (Throwable e) {
				System.err.println("Error while loading patch " + file.getAbsolutePath());
				e.printStackTrace();
				return e;
			}
		}

		classMetaCache.putAll(PATCHES);
		System.out.println("Loaded " + loadedPatches + " GrieferUtils patch(es)");

		return null;
	}

	private static class PatchLoader extends URLClassLoader {

		public PatchLoader() {
			super(new URL[0], PatchFileProvider.class.getClassLoader());
			if (LABY_3.isActive()) {
				try {
					// Copy packages (and their info)
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

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			ClassMeta patch = PATCHES.get(name.replace('.', '/') + ".class");
			return patch == null ? super.loadClass(name) : patch.load();
		}

	}

}
