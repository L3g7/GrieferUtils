package dev.l3g7.griefer_utils;

import dev.l3g7.griefer_utils.event.events.OnEnable;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.misc.UpdateCheck;
import dev.l3g7.griefer_utils.util.Reflection;
import dev.l3g7.griefer_utils.util.VersionUtil;
import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.SettingsElement;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.List;

public class Main extends LabyModAddon { //TODO: WebASM, Look through all classes :>

    private static Main instance;

    public static Main getInstance() {
        return instance;
    }

    public Main() {
        instance = this;
    }

    public void onEnable() {
        if (VersionUtil.isForge()) {
            FileProvider.loadLateLoadPackages();
            FileProvider.callAllAnnotatedMethods(OnEnable.class);
        }
    }

    public void loadConfig() {
        UpdateCheck.checkForUpdate(about.uuid);
    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {
        if (VersionUtil.isForge()) {
            // Load MainPage by Reflection, so it doesn't get loaded if isForge is false
            list.addAll(Reflection.get(Reflection.loadClass("dev.l3g7.griefer_utils.settings.MainPage"), "settings"));
        }
    }

}
