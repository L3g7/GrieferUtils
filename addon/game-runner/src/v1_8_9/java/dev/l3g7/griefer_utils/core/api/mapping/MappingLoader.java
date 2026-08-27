package dev.l3g7.griefer_utils.core.api.mapping;

import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.post_processor.LatePostProcessor;
import dev.l3g7.griefer_utils.post_processor.processors.MixinShadowRemapper;
import dev.pymdk.mapper.FastMapper;
import dev.pymdk.mapper.Mapping;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipInputStream;

import static dev.l3g7.griefer_utils.core.api.util.CryptUtil.checkHashFail;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class MappingLoader {

	private static final String ZIP_MAPPINGS_URL = "http://maven.pymdk.dev/dev/pymdk/mappings/1.8.9/mappings-1.8.9-mcp.json.zip";
	private static final String ZIP_MAPPINGS_HASH = "iJtxxul6dUe9f4mYzZdRyKcyEcn8v8vDISnlG+lWfqg=";
	private static final String MAPPINGS_HASH = "JMqZhVdaZJrghxBHoriSMSwmGRU6z3U+QufZlid+8FM=";

	static void loadMappings(Path assetsDir, boolean registerPostProcessor) {
		Path zipMappings = assetsDir.resolve("griefer_utils/mappings/pymdk-1.8.9.json.xz");
		Path mappings = assetsDir.resolve("griefer_utils/mappings/pymdk-1.8.9.json");

		try {
			// Check compressed mappings
			if (!Files.exists(zipMappings) || checkHashFail(zipMappings, ZIP_MAPPINGS_HASH))
				downloadMappings(zipMappings);

			// Check decompressed mappings
			if (!Files.exists(mappings) || checkHashFail(mappings, MAPPINGS_HASH))
				decompressMappings(zipMappings, mappings);

			// Load mappings
			FastMapper.configure(Mapping.UNOBFUSCATED, Mapping.INTERMEDIARY);
			FastMapper.loadMappings(mappings);
		} catch (IOException e) {
			throw Util.elevate(e);
		}

		if (registerPostProcessor) {
			LatePostProcessor.mappingTransformer = (name, transformedName, classBytes) -> FastMapper.mapClass(classBytes).getData();
			LatePostProcessor.processors.add(0, new MixinShadowRemapper());
		}
	}

	private static void downloadMappings(Path xzMappings) throws IOException {
		// Download mappings
		Files.createDirectories(xzMappings.getParent());

		URLConnection c = URI.create(ZIP_MAPPINGS_URL).toURL().openConnection();
		c.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
		try (InputStream in = c.getInputStream()) {
			Files.copy(in, xzMappings, REPLACE_EXISTING);
		}

		if (checkHashFail(xzMappings, ZIP_MAPPINGS_HASH))
			// Downloading failed
			throw new IOException("XZ Mappings file has an invalid hash!");
	}

	private static void decompressMappings(Path xzMappings, Path mappings) throws IOException {
		try (InputStream fileIn = Files.newInputStream(xzMappings);
		     ZipInputStream zipIn = new ZipInputStream(fileIn);
		     OutputStream fileOut = Files.newOutputStream(mappings)) {
			zipIn.getNextEntry();

			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = zipIn.read(buffer)) != -1) {
				fileOut.write(buffer, 0, bytesRead);
			}
		}

		if (checkHashFail(mappings, MAPPINGS_HASH))
			// Decompressing failed
			throw new IOException("Mappings file has an invalid hash!");
	}

}
