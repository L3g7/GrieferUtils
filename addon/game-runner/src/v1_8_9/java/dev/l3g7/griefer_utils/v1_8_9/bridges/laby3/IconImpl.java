package dev.l3g7.griefer_utils.v1_8_9.bridges.laby3;

import dev.l3g7.griefer_utils.api.misc.Citybuild;
import dev.l3g7.griefer_utils.laby3.settings.Icon;
import dev.l3g7.griefer_utils.laby4.settings.ItemStackIcon;
import dev.l3g7.griefer_utils.v1_8_9.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.v1_8_9.util.render.GlEngine;
import net.labymod.main.LabyMod;
import net.labymod.utils.Material;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import static dev.l3g7.griefer_utils.api.misc.Citybuild.CitybuildIconBridge.citybuildIconBridge;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

public class IconImpl {

	public static Icon of(Object icon) {
		if (icon == null)
			return null;

		if (icon instanceof Material m) {
			// TODO don't use materials
			return of(m.createItemStack());
		}
		if (icon instanceof String)
			return new TexturedIcon(new ResourceLocation("griefer_utils", "icons/" + icon + ".png"));
		else if (icon instanceof Citybuild citybuild)
			return of(citybuildIconBridge.toItemStack(citybuild));
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
		public void draw(int x, int y, float scale) {
			GlStateManager.enableBlend();
			GlStateManager.color(1, 1, 1);
			mc().getTextureManager().bindTexture(location);
			DrawUtils.drawTexture(x + 3, y + 3, 0, 0, 256, 256, 16 * scale, 16 * scale, 2);
		}

	}

	private static class ItemStackIcon extends Icon {

		private final ItemStack stack;

		public ItemStackIcon(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public void draw(int x, int y, float scale) {
			GlEngine.begin();
			GlEngine.scale(scale);
			DrawUtils.drawItem(stack, x + 3 / scale, y + 2 / scale, null);
			GlEngine.finish();
		}

	}

}
