package dev.l3g7.griefer_utils.file_provider.impl;

import dev.l3g7.griefer_utils.file_provider.FileProviderImpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * An implementation for providing files if the addon was loaded from a jar file.
 */
public class JarFileProviderImpl implements FileProviderImpl {

	private final Set<String> entries = new HashSet<>();
	private final JarFile jarFile;

	public JarFileProviderImpl() throws IOException, IllegalStateException {

		String jarPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
		if (!jarPath.contains(".jar"))
			throw new IllegalStateException("Invalid code source location: " + jarPath);

		// Sanitize jarPath
		jarPath = jarPath.substring(6, jarPath.lastIndexOf("!")); // remove protocol and class
		jarPath = URLDecoder.decode(jarPath, "UTF-8");

		// Read entries
		jarFile = new JarFile(jarPath);
		jarFile.stream().forEach(entry -> entries.add(entry.getName()));

		if (entries.isEmpty())
			throw new IllegalStateException("Empty jar file: " + jarPath);
	}

	@Override
	public Collection<String> getFiles() {
		return entries;
	}

	@Override
	public InputStream getData(String file) {
		try {
			return jarFile.getInputStream(jarFile.getJarEntry(file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
