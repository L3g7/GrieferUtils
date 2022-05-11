package dev.l3g7.griefer_utils.settings;

import dev.l3g7.griefer_utils.Main;
import dev.l3g7.griefer_utils.event.events.LateInit;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.util.IOUtil;
import dev.l3g7.griefer_utils.util.Reflection;
import dev.l3g7.griefer_utils.util.VersionUtil;
import net.labymod.addon.AddonLoader;
import net.labymod.addon.online.AddonInfoManager;
import net.labymod.addon.online.info.AddonInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;

@Singleton
public class AddonDescriptor {

    public static AddonInfo getAddonInfo() {
        if(Main.getInstance().about == null)
            Minecraft.getMinecraft().crashed(new CrashReport("GrieferUtils failed to load!", new NullPointerException("Addon's about is null")));

        AddonInfoManager addonInfoManager = AddonInfoManager.getInstance();
        addonInfoManager.init();

        if(addonInfoManager.getAddonInfoMap() == null)
            Minecraft.getMinecraft().crashed(new CrashReport("GrieferUtils failed to load!", new NullPointerException("addon info map is null")));

        AddonInfo addonInfo = addonInfoManager.getAddonInfoMap().get(Main.getInstance().about.uuid);
        if (addonInfo == null)
            addonInfo = AddonLoader.getOfflineAddons().stream().filter(addon -> addon.getUuid().equals(Main.getInstance().about.uuid)).findFirst().orElse(null);
        return addonInfo;
    }

    @LateInit
    public void init() {
        AddonInfo addonInfo = getAddonInfo();
        if (addonInfo != null) {
            Reflection.set(addonInfo, "L3g7 \u2503 v" + VersionUtil.getAddonVersion(), "author");

            // Load description from server, so it can be used as news board
            // Also sneaky way to get usage stats
            IOUtil.request("https://grieferutils.l3g7.dev/addon_description?v=" + IOUtil.urlEncode(VersionUtil.getAddonVersion()))
                    .asJsonString(v -> Reflection.set(addonInfo, v, "description"))
                    .orElse(t -> Reflection.set(addonInfo, "\u00A7cFalls du das hier liest, gab es einen Fehler. :(", "description"));
        }
    }

}
