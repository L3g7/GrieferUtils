package dev.l3g7.griefer_utils.file_provider;

import dev.l3g7.griefer_utils.file_provider.impl.JarFileProviderImpl;
import dev.l3g7.griefer_utils.file_provider.impl.URLFileProviderImpl;

import java.io.InputStream;
import java.util.Collection;

/**
 * A provider implementation.
 *
 * @see JarFileProviderImpl
 * @see URLFileProviderImpl
 */
public interface FileProviderImpl {

	Collection<String> getFiles();
	InputStream getData(String file);

}
