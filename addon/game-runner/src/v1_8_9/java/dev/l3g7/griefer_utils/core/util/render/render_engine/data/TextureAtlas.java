package dev.l3g7.griefer_utils.core.util.render.render_engine.data;

import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Predicate;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class TextureAtlas {

	protected final ResourceLocation texture;
	protected final int[] pixels;
	protected final int texWidth;
	protected final int texHeight;

	protected final int entriesX;
	protected final int entriesY;
	protected final int entryWidth;
	protected final int entryHeight;

	protected int x, y;

	public TextureAtlas(ResourceLocation texture, int entriesX, int entriesY) throws IOException {
		InputStream stream = mc().getResourceManager().getResource(texture).getInputStream();
		BufferedImage img = TextureUtil.readBufferedImage(stream);
		// TextureUtil.readBufferedImage closes the stream

		this.texture = texture;
		texWidth = img.getWidth();
		texHeight = img.getHeight();
		this.entriesX = entriesX;
		this.entriesY = entriesY;
		entryHeight = texHeight / entriesX;
		entryWidth = texWidth / entriesY;

		pixels = new int[texWidth * texHeight];
		img.getRGB(0, 0, texWidth, texHeight, pixels, 0, texWidth);
	}

	/**
	 * Converts pixel coordinates into relative UV coordinates
	 */
	protected Texture createSprite(int minX, int maxX, int minY, int maxY) {
		return new Texture(
			texture,
			minX / (double) texWidth,
			maxX / (double) texWidth,
			minY / (double) texHeight,
			maxY / (double) texHeight
		);
	}

	public Texture getSprite(int spriteIndex) {
		this.x = (spriteIndex % entriesX) * entryWidth;
		this.y = (spriteIndex / entriesY) * entryHeight;

		return createSprite(x, x + entryWidth, y, y + entryHeight);
	}

	/**
	 * Trims the UV data of each char by finding its edges
	 */
	public static class FontAtlas extends TextureAtlas {

		public FontAtlas(String pageName) throws IOException {
			super(new ResourceLocation("minecraft", "textures/font/" + pageName + ".png"), 16, 16);
		}

		@Override
		public Texture getSprite(int spriteIndex) {
			this.x = (spriteIndex % 16) * entryWidth;
			this.y = (spriteIndex / 16) * entryHeight;

			return createSprite(
				x + findTextureEdge(entryWidth, false, this::isColumnEmpty),
				x + findTextureEdge(entryWidth, true, this::isColumnEmpty),
				y + findTextureEdge(entryHeight, false, this::isLineEmpty),
				y + findTextureEdge(entryHeight, true, this::isLineEmpty)
			);
		}

		private int findTextureEdge(int end, boolean reversed, Predicate<Integer> check) {
			if (!reversed)
				for (int i = 0; i < end; i++)
					if (!check.test(i))
						return i;

			for (int i = end - 1; i >= 0; i--)
				if (!check.test(i))
					return i + 1;

			System.err.printf("Sprite at %d %d is invisible!%n", x, y);
			return reversed ? end : 0;
		}

		private boolean isColumnEmpty(int x) {
			for (int i = 0; i < entryHeight; i++)
				if (isPixelFilled(this.x + x, this.y + i))
					return false;

			return true;
		}

		private boolean isLineEmpty(int y) {
			for (int i = 0; i < entryWidth; i++)
				if (isPixelFilled(this.x + i, this.y + y))
					return false;

			return true;
		}

		private boolean isPixelFilled(int x, int y) {
			return (pixels[x + y * texWidth] >> 24) != 0;
		}

	}

}
