package dev.l3g7.griefer_utils.util;

import dev.l3g7.griefer_utils.file_provider.FileProvider;

public class VersionUtil {

    private static String addonVersion = null;
    private static final boolean isForge = Reflection.loadClass("net.minecraftforge.common.ForgeVersion") != null;

    public static boolean isForge() {
        return isForge;
    }

    public static String getAddonVersion() {
        if(addonVersion == null) {
            addonVersion = IOUtil.JSON_PARSER.parse(new String(FileProvider.getProvider().getData().get("addon.json"))).getAsJsonObject().get("addonVersion").getAsString();
            if(addonVersion.equals("${version}")) {
                addonVersion = "DEBUG";
            }
        }
        return addonVersion;
    }

}
