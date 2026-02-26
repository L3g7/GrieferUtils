package dev.l3g7.griefer_utils.features.uncategorized.debug.wiki;

import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.api.util.IOUtil;
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.features.Feature;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

import static org.objectweb.asm.ClassReader.SKIP_CODE;

/**
 * {@link FileProvider} without exclusion checks.
 */
public class UncheckedFileProvider {

	protected static final Map<String, ClassMeta> classMetaCache = new HashMap<>();

	/**
	 * @see Feature#getFeatures()
	 */
	public static Stream<ClassMeta> getFeatures() {
		return getClassesWithSuperClass(Feature.class).stream()
			.filter(m -> !m.isAbstract());
	}

	/**
	 * @see FileProvider#getClassesWithSuperClass(Class)
	 */
	public static Collection<ClassMeta> getClassesWithSuperClass(Class<?> superClass) {
		return getClassesWithSuperClass(Type.getInternalName(superClass));
	}

	/**
	 * @see FileProvider#getClassesWithSuperClass(String)
	 */
	public static Collection<ClassMeta> getClassesWithSuperClass(String superClass) {
		List<ClassMeta> classes = new ArrayList<>();

		// Find classes
		for (String file : FileProvider.getFiles(f -> f.endsWith(".class"))) {
			ClassMeta meta = getClassMeta(file, false);
			if (meta != null && meta.hasSuperClass(superClass))
				classes.add(meta);
		}

		return classes;
	}

	/**
	 * @see FileProvider#getClassMeta(String, boolean)
	 */
	public static ClassMeta getClassMeta(String file, boolean loadUnknownFiles) {
		if (classMetaCache.containsKey(file))
			return classMetaCache.get(file);

		// Check if file is known
		if (!FileProvider.getFiles().contains(file)) {
			if (!loadUnknownFiles)
				return null;

			// Load ClassMeta using Reflection
			ClassMeta meta = new ClassMeta(Reflection.load(file));
			classMetaCache.put(file, meta);
			return meta;
		}

		if (!file.endsWith(".class"))
			throw new IllegalArgumentException("Cannot load class meta of " + file);

		// Load ClassMeta using ASM
		try (InputStream in = FileProvider.getData(file)) {
			ClassNode node = new ClassNode();
			byte[] bytes = IOUtil.toByteArray(in);
			bytes[7 /* major_version */] = (byte) Math.min(bytes[7], 52 /* Java 1.8 */);
			new ClassReader(bytes).accept(node, SKIP_CODE);

			ClassMeta meta = new ClassMeta(node);
			classMetaCache.put(file, meta);
			return meta;
		} catch (IOException e) {
			throw Util.elevate(e, "Tried to read class meta of " + file);
		}
	}

}
