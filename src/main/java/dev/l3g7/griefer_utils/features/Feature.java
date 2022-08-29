package dev.l3g7.griefer_utils.features;

import dev.l3g7.griefer_utils.event.event_bus.EventBus;
import dev.l3g7.griefer_utils.event.events.chat.MessageSendEvent;
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
    public static boolean isOnGrieferGames() { return ServerCheck.isOnGrieferGames(); }
    public static boolean isOnCityBuild() { return ServerCheck.isOnCitybuild(); }

    public static Minecraft mc() { return Minecraft.getMinecraft(); }
    public static EntityPlayerSP player() { return mc().thePlayer; }
    public static WorldClient world() { return mc().theWorld; }
    public static UUID uuid() { return PlayerUtil.getUUID(); }
    public static String name() { return PlayerUtil.getName(); }

    public static void display(IChatComponent component) { player().addChatMessage(component); }
    public static void display(String msg) { LabyMod.getInstance().displayMessageInChat(msg); }
    public static void display(String format, Object... args) { display(String.format(format, args)); }

    public static void suggest(String cmd) { mc().displayGuiScreen(new GuiChat(cmd)); }
    public static void suggest(String format, Object... args) { suggest(String.format(format, args)); }

    public static void send(String cmd) { if (!EventBus.post(new MessageSendEvent(cmd)).isCanceled()) player().sendChatMessage(cmd); }
    public static void send(String format, Object... args) { send(String.format(format, args)); }

    public static void sendQueued(String cmd) { TickScheduler.queue("chat", () -> { if(player() != null) send(cmd); }, 50); }

    public static void displayAchievement(String title, String description, Object... args) {
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
