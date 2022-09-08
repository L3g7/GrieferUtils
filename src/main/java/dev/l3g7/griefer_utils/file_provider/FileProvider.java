package dev.l3g7.griefer_utils.file_provider;

import dev.l3g7.griefer_utils.file_provider.impl.JarFileProviderImpl;
import dev.l3g7.griefer_utils.file_provider.impl.URLFileProviderImpl;
import dev.l3g7.griefer_utils.util.reflection.Reflection;

import java.util.ArrayList;
import java.util.List;

/**
 * A class providing a list of all files in the addon.
 */
public class FileProvider {

	private static final Class<?>[] PROVIDERS = new Class[]{JarFileProviderImpl.class, URLFileProviderImpl.class};

	private static FileProviderImpl impl = null;

	/**
	 * Lazy loads the implementation if required and returns it.
	 */
	private static FileProviderImpl getProvider() {
		if (impl != null)
			return impl;

		List<Throwable> errors = new ArrayList<>();
		for (Class<?> providerClass : PROVIDERS) {
			try {
				return impl = (FileProviderImpl) Reflection.construct(providerClass);
			} catch (Throwable throwable) {
				errors.add(throwable);
			}
		}

		// Only throw errors if no implementation could load
		errors.forEach(Throwable::printStackTrace);
		throw new RuntimeException("No available file provider could be found!");
	}

}
