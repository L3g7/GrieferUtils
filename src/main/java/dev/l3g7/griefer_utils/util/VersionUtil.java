package dev.l3g7.griefer_utils.util;

import dev.l3g7.griefer_utils.file_provider.FileProvider;

public class VersionUtil {

    private static String addonVersion = null;

    public static String getAddonVersion() {
        if(addonVersion == null) {
            byte[] addonJson = FileProvider.getProvider().getData().get("addon.json");
            if(addonJson == null) {
                addonVersion = "DEBUG";
            } else {
                addonVersion = IOUtil.JSON_PARSER.parse(new String(addonJson)).getAsJsonObject().get("addonVersion").getAsString();
                if(addonVersion.equals("${version}")) {
                    addonVersion = "DEBUG";
                }
            }
        }
        return addonVersion;
    }

}
