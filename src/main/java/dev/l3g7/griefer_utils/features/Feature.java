package dev.l3g7.griefer_utils.features;

import dev.l3g7.griefer_utils.features.tweaks.CommandLogger;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.misc.TickScheduler;
import dev.l3g7.griefer_utils.settings.MainPage;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.PlayerUtil;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.IChatComponent;

import java.util.UUID;

public abstract class Feature implements Comparable<Feature> {

    private final Category category;

    public Feature(Category category) { this.category = category; }
    public Category getCategory() { return category; }

    public abstract SettingsElement getMainElement();

    public boolean isActive() {
        return ((BooleanSetting) getMainElement()).get() && isCategoryEnabled();
    }

    public boolean isCategoryEnabled() { return category.setting.get(); }
    public boolean isOnGrieferGames() { return ServerCheck.isOnGrieferGames(); }
    public boolean isOnCityBuild() { return ServerCheck.isOnCitybuild(); }

    public Minecraft mc() { return Minecraft.getMinecraft(); }
    public EntityPlayerSP player() { return mc().thePlayer; }
    public WorldClient world() { return mc().theWorld; }
    public UUID uuid() { return PlayerUtil.getUUID(); }

    public void display(IChatComponent component) { player().addChatMessage(component); }
    public void display(String msg) { LabyMod.getInstance().displayMessageInChat(msg); }
    public void display(String format, Object... args) { display(String.format(format, args)); }

    public void suggest(String cmd) { mc().displayGuiScreen(new GuiChat(cmd)); }
    public void suggest(String format, Object... args) { suggest(String.format(format, args)); }

    public void send(String cmd) {
        if (FileProvider.getSingleton(CommandLogger.class).isActive())
            CommandLogger.log(cmd);

        player().sendChatMessage(cmd);
    }
    public void send(String format, Object... args) { send(String.format(format, args)); }

    public void sendQueued(String cmd) { TickScheduler.queue("chat", () -> { if(player() != null) send(cmd); }, 50); }

    public void displayAchievement(String title, String description, Object... args) {
        LabyMod.getInstance().getGuiCustomAchievement().displayAchievement("https://grieferutils.l3g7.dev/icon/64x64/", title, String.format(description, args));
    }

    @Override
    public int compareTo(Feature feature) {
        return getMainElement().getDisplayName().compareToIgnoreCase(feature.getMainElement().getDisplayName());
    }

    public enum Category {

        FEATURE(MainPage.features),
        TWEAK(MainPage.tweaks),
        MISC(null);

        public final BooleanSetting setting;

        Category(BooleanSetting setting) {
            this.setting = setting;
        }

    }
}
