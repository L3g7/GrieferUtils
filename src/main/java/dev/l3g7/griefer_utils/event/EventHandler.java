package dev.l3g7.griefer_utils.event;

import dev.l3g7.griefer_utils.event.event_bus.EventBus;
import dev.l3g7.griefer_utils.event.events.chat.ChatLineAddEvent;
import dev.l3g7.griefer_utils.event.events.network.PacketSendEvent;
import dev.l3g7.griefer_utils.event.events.render.*;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.util.IChatComponent;

/**
 * Handles the logic for events called by ASM
 */
@Singleton
@SuppressWarnings("unused")
public class EventHandler {

    // Called by EntityEditor (editIsBurning)
    public static boolean shouldNotBeBurning(Entity entity) {
        return EventBus.post(new RenderBurningCheckEvent(entity)).isCanceled();
    }

    // Called by EntityEditor, EntityPlayerEditor (editIsInvisibleToPlayer)
    public static boolean shouldBeVisible(Entity entity) {
        return EventBus.post(new RenderInvisibilityCheckEvent(entity)).shouldRender();
    }

    // Called by EntityPlayerEditor (editGetDisplayName)
    public static IChatComponent modifyDisplayName(IChatComponent component, EntityPlayer player) {
        return EventBus.post(new DisplayNameRenderEvent(component, player)).getDisplayName();
    }

    // Called by EntityRendererEditor (editSetupFog)
    public static boolean shouldRenderFog(int type) {
        return !EventBus.post(new RenderFogCheckEvent(type)).isCanceled();
    }

    // Called by ClientWorldEditor (editVoidFog)
    public static boolean shouldRenderBarrier() {
        return !EventBus.post(new RenderBarrierCheckEvent()).isCanceled();
    }

	// Called by NetHandlerPlayClientEditor (editAddToSendQueue)
	public static boolean shouldSendPacket(Packet<?> packet) {
		return !EventBus.post(new PacketSendEvent(packet)).isCanceled();
	}

	// Called by GuiContainerEditor (editDrawGuiContainerForegroundLayer)
	public static void drawGuiContainerForegroundLayer(GuiChest chest) {
		EventBus.post(new DrawGuiContainerForegroundLayerEvent(chest));
	}

	// Called by ChatRendererEditor (editAddChatLine)
	public static void addChatLine(String message, boolean refresh) {
		ChatLineAddEvent.onLineAdd(message, refresh);
	}

}
