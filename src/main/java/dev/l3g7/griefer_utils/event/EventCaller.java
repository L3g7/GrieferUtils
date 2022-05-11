package dev.l3g7.griefer_utils.event;

import dev.l3g7.griefer_utils.Main;
import dev.l3g7.griefer_utils.event.event_bus.EventBus;
import dev.l3g7.griefer_utils.event.events.LateInit;
import dev.l3g7.griefer_utils.event.events.SettingsUpdateEvent;
import dev.l3g7.griefer_utils.event.events.chat.MessageModifyEvent;
import dev.l3g7.griefer_utils.event.events.chat.MessageReceiveEvent;
import dev.l3g7.griefer_utils.event.events.chat.MessageSendEvent;
import dev.l3g7.griefer_utils.event.events.network.PacketReceiveEvent;
import dev.l3g7.griefer_utils.event.events.render.EntityRenderEvent;
import dev.l3g7.griefer_utils.event.events.server.CityBuildJoinEvent;
import dev.l3g7.griefer_utils.event.events.server.ServerJoinEvent;
import dev.l3g7.griefer_utils.event.events.server.ServerQuitEvent;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.AddonDescriptor;
import dev.l3g7.griefer_utils.util.Reflection;
import net.labymod.api.events.MessageModifyChatEvent;
import net.labymod.labyconnect.packets.PacketAddonDevelopment;
import net.labymod.main.LabyMod;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.AddonElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.Packet;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Handles the stuff for calling events
 * (Except for events called by ASM (see EventHandler) and events based on other events)
 */
@Singleton
public class EventCaller {

    public EventCaller() {
        MinecraftForge.EVENT_BUS.register(this);
        EventBus.register(this);

        // Register events based on LabyMod (ServerJoin, ServerQuit, MessageSend, MessageModify, EntityRender, PacketReceive)
        LabyMod.getInstance().getEventManager().registerOnJoin(data -> EventBus.post(new ServerJoinEvent(data)));
        LabyMod.getInstance().getEventManager().registerOnQuit(data -> EventBus.post(new ServerQuitEvent(data)));
        LabyMod.getInstance().getEventManager().register((net.labymod.api.events.MessageSendEvent) msg -> EventBus.post(new MessageSendEvent(msg)).isCanceled());
        LabyMod.getInstance().getEventManager().register((MessageModifyChatEvent) o -> EventBus.post(new MessageModifyEvent((IChatComponent) o)).getMessage());
        LabyMod.getInstance().getEventManager().register((entity, x, y, z, pTicks) -> EventBus.post(new EntityRenderEvent(entity, x, y, z)));
        LabyMod.getInstance().getEventManager().registerOnIncomingPacket(p -> EventBus.post(new PacketReceiveEvent((Packet<?>) p)));
    }

    @SubscribeEvent
    public void onMessage(ClientChatReceivedEvent event) {
        if (event.type == 2)
            return;

        // CityBuildJoinEvent
        if (event.message.getFormattedText().equals("§r§8[§r§6GrieferGames§r§8] §r§aDeine Daten wurden vollständig heruntergeladen.§r"))
            EventBus.post(new CityBuildJoinEvent());

        // MessageReceiveEvent
        event.setCanceled(EventBus.post(new MessageReceiveEvent(event.message)).isCanceled());
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;

        // SettingsUpdateEvent
        if (gui instanceof LabyModAddonsGui) {
            AddonElement openedAddon = Reflection.get(gui, "openedAddonSettings");
            if (openedAddon != null && openedAddon.getInstalledAddon().equals(Main.getInstance()))
                EventBus.post(new SettingsUpdateEvent(Reflection.get(gui, "tempElementsStored")));
        }
    }

    private boolean initialized = false;

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (initialized) return;
        if (!(event.gui instanceof GuiMainMenu)) return;

        // @LateInit
        FileProvider.callAllAnnotatedMethods(LateInit.class);
        LabyMod.getInstance().getEventManager().callAddonDevelopmentPacket(new PacketAddonDevelopment(AddonDescriptor.getAddonInfo().getUuid(), "griefer_utils", new byte[]{4, 105, 110, 105, 116}));
        initialized = true;
    }
}
