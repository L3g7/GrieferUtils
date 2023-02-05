package dev.l3g7.griefer_utils.injection.mixin;

import dev.l3g7.griefer_utils.event.events.render.RenderItemOverlayEvent;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderItem.class)
public abstract class MixinRenderItem {

	@Shadow
	protected abstract void draw(WorldRenderer renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha);

	@Inject(method = "renderItemOverlayIntoGUI", at = @At("TAIL"))
	public void injectRenderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text, CallbackInfo ci) {
		MinecraftForge.EVENT_BUS.post(new RenderItemOverlayEvent((RenderItem) (Object) this, stack, xPosition, yPosition));

	}

}
