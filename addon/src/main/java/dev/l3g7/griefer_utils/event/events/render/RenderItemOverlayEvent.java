package dev.l3g7.griefer_utils.event.events.render;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Event;

public class RenderItemOverlayEvent extends Event {

	public final ItemStack stack;
	public final RenderItem renderItem;
	public final int x;
	public final int y;

	public RenderItemOverlayEvent(RenderItem renderItem, ItemStack stack, int x, int y) {
		this.stack = stack;
		this.renderItem = renderItem;
		this.x = x;
		this.y = y;
	}

}
