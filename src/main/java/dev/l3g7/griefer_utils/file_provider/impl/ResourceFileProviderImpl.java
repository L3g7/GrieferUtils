package dev.l3g7.griefer_utils.file_provider.impl;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ResourceFileProviderImpl implements FileProviderImpl { // When running in IDE

    private final boolean isAvailable;

    private final Map<String, byte[]> data = new HashMap<>();

    public ResourceFileProviderImpl() {
        try {
            // Read classes using ClassLoader
            if(ClassLoader.getSystemClassLoader() != null) {
                try (InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("")) {
                    if(is != null)
                        process("");
                }
            }
        } catch(Throwable t) {
            t.printStackTrace();
            isAvailable = false;
            return;
        }
        isAvailable = !data.isEmpty();
    }

    private void process(String path) throws IOException, NullPointerException {
        try(InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(path)) {
            if(stream == null) return;

            if(stream instanceof BufferedInputStream) // File
                data.put(path, IOUtils.toByteArray(stream));
            else // Directory
                for(String line : IOUtils.readLines(stream))
                    process(path.isEmpty() ? line : path + "/" + line);
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
