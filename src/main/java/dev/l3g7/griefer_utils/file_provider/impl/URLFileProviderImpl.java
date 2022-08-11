package dev.l3g7.griefer_utils.file_provider.impl;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class URLFileProviderImpl implements FileProviderImpl { // When running in IDE

	private final boolean isAvailable;

	private final Map<String, byte[]> data = new HashMap<>();

	public URLFileProviderImpl() {
		try {
			for (URL url : ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs()) {
				File root = new File(url.toURI());
				if (root.isDirectory())
					load(root, root);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			isAvailable = false;
			return;
		}
		isAvailable = !data.isEmpty();
	}

	private void load(File root, File directory) throws Throwable {
		for (File file : directory.listFiles()) {
			if (file.isDirectory())
				load(root, file);
			else {
				data.put(file.getCanonicalPath().substring(root.getCanonicalPath().length() + 1).replace('\\', '/'), null);
			}
		}
	}

	@Override
	public Map<String, byte[]> getData() {
		return data;
	}

	@Override
	public boolean isAvailable() {
		return isAvailable;
	}

}
