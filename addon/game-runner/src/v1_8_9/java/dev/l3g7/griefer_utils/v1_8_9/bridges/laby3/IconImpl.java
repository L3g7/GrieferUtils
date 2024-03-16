package dev.l3g7.griefer_utils.v1_8_9.bridges.laby3;

import dev.l3g7.griefer_utils.laby3.settings.Icon;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class IconImpl {

	public static Icon of(Object icon) {
		if (icon == null)
			return null;

		if (icon instanceof String)
			return new TexturedIcon(new ResourceLocation("griefer_utils", "icons/" + icon + ".png"));
		else if (icon instanceof ResourceLocation location)
			return new TexturedIcon(location);
		else if (icon instanceof Icon i)
			return i;
		else if (icon instanceof ItemStack stack)
			return new ItemStackIcon(stack);
		else if (icon instanceof Item item)
			return new ItemStackIcon(new ItemStack(item));
		else if (icon instanceof Block block)
			return new ItemStackIcon(new ItemStack(block));

		throw new UnsupportedOperationException(icon.getClass().getSimpleName() + " is an unsupported icon type!");
	}

	private static class TexturedIcon extends Icon {

		private final ResourceLocation location;

		public TexturedIcon(ResourceLocation location) {
			this.location = location;
		}

		@Override
		public void draw(int x, int y) {
			throw new UnsupportedOperationException(location.toString()); // TODO
		}

	}

	private static class ItemStackIcon extends Icon {

		private final ItemStack stack;

		public ItemStackIcon(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public void draw(int x, int y) {
			throw new UnsupportedOperationException(stack.toString()); // TODO
		}

	}

}
