package dev.l3g7.griefer_utils.file_provider.impl;

import net.labymod.core.asm.LabyModCoreMod;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JarFileProviderImpl implements FileProviderImpl { // When running in Launcher

    private final boolean isAvailable;

    private final Map<String, byte[]> data = new HashMap<>();

    public JarFileProviderImpl() {
        String jarPath = null;
        try {
            jarPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
            if (jarPath.contains(".jar")) {

                // Sanitize jarPath
                jarPath = jarPath.substring(5, jarPath.lastIndexOf("!")); // remove protocol and class
                jarPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8.name());

                // Read jar file as zip
                File jarFile = new File(jarPath);
                if (jarFile.exists()) {
                    try(ZipFile zipFile = new ZipFile(jarFile)) {
                        for (ZipEntry entry : Collections.list(zipFile.entries())) {
                            if (entry.isDirectory())
                                continue;

                            data.put(entry.getName(), IOUtils.toByteArray(zipFile.getInputStream(entry)));
                        }
                    }
                }
            } else if (LabyModCoreMod.isObfuscated()) // Only log error if in obfuscated environment
                System.err.println("Jar path doesn't contain .jar");
        } catch (Throwable t) {
            System.err.println("JarPath: " + jarPath);
            t.printStackTrace();
            isAvailable = false;
            return;
        }
        isAvailable = !data.isEmpty();
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
