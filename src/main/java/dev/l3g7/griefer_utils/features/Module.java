package dev.l3g7.griefer_utils.features;

import dev.l3g7.griefer_utils.event.events.OnEnable;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.ingamegui.ModuleCategory;
import net.labymod.ingamegui.ModuleCategoryRegistry;
import net.labymod.ingamegui.moduletypes.SimpleTextModule;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;

public abstract class Module extends SimpleTextModule {

    public static final ModuleCategory CATEGORY = new ModuleCategory(Constants.ADDON_NAME, true, new ControlElement.IconData("griefer_utils/icons/icon.png"));

    @OnEnable
    public static void register() {
        ModuleCategoryRegistry.loadCategory(CATEGORY);
        FileProvider.getAllClasses()
                .filter(c -> Reflection.hasSuperclass(c, Module.class))
                .map(Reflection::loadClass)
                .map(Module.class::cast)
                .sorted((a, b) -> (a.getClass().getPackage().getName() + a.getControlName()).compareToIgnoreCase((b.getClass().getPackage().getName() + b.getControlName()))) // Include package in sorting so the modules are grouped
                .forEach(LabyMod.getInstance().getLabyModAPI()::registerModule);
    }

    private final String name;
    private final String description;
    private final String configKey;
    private final ControlElement.IconData iconData;

    public Module(String name, String description, String configKey, ControlElement.IconData iconData) {
        this.name = name;
        this.description = description;
        this.configKey = configKey;
        this.iconData = iconData;
    }

    public String getControlName() { return name; }

    public String[] getKeys() { return new String[]{name}; }
    public String[] getDefaultKeys() { return new String[]{name}; }

    public ControlElement.IconData getIconData() { return iconData; }
    public String getSettingName() { return configKey; }
    public String getDescription() { return description; }
    public boolean isShown() { return !LabyMod.getInstance().isInGame() || ServerCheck.isOnGrieferGames(); }
    public boolean isActive() { return getBooleanElement().getCurrentValue(); }

    public void loadSettings() {}
    public int getSortingId() { return 0; }
    public ModuleCategory getCategory() { return CATEGORY; }

}
