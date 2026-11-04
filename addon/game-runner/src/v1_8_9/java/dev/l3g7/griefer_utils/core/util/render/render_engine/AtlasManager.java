package dev.l3g7.griefer_utils.core.util.render.render_engine;

import com.google.common.collect.ImmutableMap;
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.TextureCutout;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.UV;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

class AtlasManager {

	record Sprite(int index, int width, int height, int[] pixels) {}

	public static class UploadedAtlas {

		private final Map<TextureCutout, Sprite> sprites;
		private final UV[] slots;
		private final int textureId;

		UploadedAtlas(Map<TextureCutout, Sprite> sprites, UV[] slots, int textureId) {
			this.sprites = sprites;
			this.slots = slots;
			this.textureId = textureId;
		}

		public static UploadedAtlas empty() {
			return new UploadedAtlas(ImmutableMap.of(), new UV[0], 0);
		}

		void delete() {
			if (textureId != 0)
				TextureUtil.deleteTexture(textureId);
		}

		public void bind() {
			if (textureId != 0)
				GlStateManager.bindTexture(textureId);
		}

		public UV getUV(TextureCutout cutout) {
			return slots[sprites.get(cutout).index];
		}
	}

	private static final int PADDING = 1;

	private static final Map<ResourceLocation, BufferedImage> imageCache = new HashMap<>();
	private static final Map<TextureCutout, Sprite> sprites = new HashMap<>();
	private static int area = 0;
	private static int atlasWidth = 1;

	private static boolean isOutdated = true;
	private static UploadedAtlas uploadedAtlas = UploadedAtlas.empty();

	static boolean isOutdated() {
		return isOutdated;
	}

	static UploadedAtlas getAtlas() {
		return uploadedAtlas;
	}

	public static void reset() {
		imageCache.clear();
		sprites.clear();
		area = 0;
		atlasWidth = 1;
		isOutdated = true;
		uploadedAtlas = UploadedAtlas.empty();
	}

	/**
	 * Adds the given cutout to this atlas.
	 * If the current atlas did not already contain the cutout, it's marked outdated.
	 */
	public static void register(TextureCutout cutout) {
		if (sprites.containsKey(cutout))
			return;

		BufferedImage image = imageCache.computeIfAbsent(cutout.texture(), location -> Util.tryFatal(() -> {
			return TextureUtil.readBufferedImage(mc().getResourceManager().getResource(location).getInputStream());
		}));
		int maxX = Math.min(cutout.maxX(), image.getWidth());
		int maxY = Math.min(cutout.maxY(), image.getHeight());
		int width = maxX - cutout.minX();
		int height = maxY - cutout.minY();

		area += (width + PADDING) * (height + PADDING);
		atlasWidth = Math.max(atlasWidth, width);

		sprites.put(cutout, new Sprite(
			sprites.size(),
			width, height,
			image.getRGB(cutout.minX(), cutout.minY(), width, height, null, 0, width)
		));

		isOutdated = true;
	}

	/**
	 * Packs all sprites into a single combined image using the FFDH Strip Packing algorithm.
	 */
	public static void recompile() {
		atlasWidth = Math.max(atlasWidth, (int) Math.ceil(Math.sqrt(area)));

		List<Sprite> spritesDH = sprites.values().stream()
			.sorted(Comparator.comparingInt(Sprite::height).reversed())
			.toList();

		int[] slotX = new int[spritesDH.size()];
		int[] slotY = new int[spritesDH.size()];
		int[] rowX = new int[spritesDH.size() /* probably not used completely, but is the upper limit */];
		int[] rowY = new int[spritesDH.size() /* probably not used completely, but is the upper limit */];
		int rowCount = 0;
		int nextRowY = 0;

		// Distribute sprites
		for (Sprite sprite : spritesDH) {
			int row = 0;
			while (row < rowCount && rowX[row] + sprite.width() > atlasWidth)
				row++;

			if (row == rowCount) {
				rowCount++;
				rowY[row] = nextRowY;
				nextRowY += sprite.height() + PADDING;
			}

			slotX[sprite.index] = rowX[row];
			slotY[sprite.index] = rowY[row];
			rowX[row] += sprite.width() + PADDING;
		}

		// Render sprites into the final atlas
		int atlasHeight = Math.max(nextRowY - PADDING, 1);
		BufferedImage atlas = new BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_INT_ARGB);
		UV[] slots = new UV[spritesDH.size()];

		for (Sprite sprite : spritesDH) {
			int minX = slotX[sprite.index];
			int minY = slotY[sprite.index];

			atlas.setRGB(minX, minY, sprite.width(), sprite.height(), sprite.pixels(), 0, sprite.width());
			slots[sprite.index] = new UV(
				minX / (double) atlasWidth,
				(minX + sprite.width()) / (double) atlasWidth,
				minY / (double) atlasHeight,
				(minY + sprite.height()) / (double) atlasHeight
			);
		}

		int textureId = TextureUtil.glGenTextures();

		TextureUtil.uploadTextureImageAllocate(textureId, atlas, false /* blur */, false /* clamp uv */);
		uploadedAtlas.delete();
		uploadedAtlas = new UploadedAtlas(ImmutableMap.copyOf(sprites), slots, textureId);
		isOutdated = false;
	}

}
