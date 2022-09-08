package dev.l3g7.griefer_utils.file_provider.impl;

import dev.l3g7.griefer_utils.file_provider.FileProviderImpl;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarFile;

/**
 * An implementation for providing files if the addon can be found in the system classloader's urls.
 */
public class URLFileProviderImpl implements FileProviderImpl {

	private final Set<String> entries = new HashSet<>();
	private final Map<String, File> files = new HashMap<>();

	public URLFileProviderImpl() throws IOException, URISyntaxException {
		for (URL url : ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs()) {
			File root = new File(url.toURI());
			load(root, root);
		}

		if (entries.isEmpty())
			throw new IllegalStateException("No entries found");
	}

	private void load(File root, File file) throws IOException {
		if (file.isDirectory())
			for (File child : file.listFiles())
				load(root, child);
		else
			// Strip root path and normalize string
			files.put(file.getCanonicalPath().substring(root.getCanonicalPath().length() + 1).replace('\\', '/'), file);
	}

	@Override
	public Collection<String> getFiles() {
		return entries;
	}

	@Override
	public InputStream getData(String file) {
		try {
			return new FileInputStream(files.get(file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
