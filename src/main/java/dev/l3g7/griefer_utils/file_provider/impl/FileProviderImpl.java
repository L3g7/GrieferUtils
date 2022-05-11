package dev.l3g7.griefer_utils.file_provider.impl;

import java.util.Map;

public interface FileProviderImpl {

    Map<String, byte[]> getData();

    boolean isAvailable();

}
