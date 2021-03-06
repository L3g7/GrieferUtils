package dev.l3g7.griefer_utils.event;

import dev.l3g7.griefer_utils.event.event_bus.EventBus;
import dev.l3g7.griefer_utils.event.events.render.DisplayNameRenderEvent;
import dev.l3g7.griefer_utils.event.events.render.RenderBurningCheckEvent;
import dev.l3g7.griefer_utils.event.events.render.RenderFogCheckEvent;
import dev.l3g7.griefer_utils.event.events.render.RenderInvisibilityCheckEvent;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
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

}
