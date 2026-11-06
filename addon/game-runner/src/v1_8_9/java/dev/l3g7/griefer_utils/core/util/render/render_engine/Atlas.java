package dev.l3g7.griefer_utils.core.util.render.render_engine;

import com.google.common.collect.ImmutableMap;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.core.events.render.RefreshResourcesEvent;
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

/**
 * Stitches multiple textures into one atlas.
 */
class Atlas {

	/**
	 * The padding between individual textures in pixels
	 */
	private static final int PADDING = 1;

	private static final Map<ResourceLocation, BufferedImage> imageCache = new HashMap<>();
	/**
	 * All textures in this atlas.
	 */
	private static final Map<TextureCutout, Texture> textures = new HashMap<>();

	/**
	 * The sum of the area of all textures in {@link textures}.
	 */
	private static int cumulatedArea = 0;
	private static int minAtlasWidth = 1;

	/**
	 * Whether a new texture was added since the last atlas was uploaded.
	 */
	private static boolean isOutdated = true;
	/**
	 * The latest {@link UploadedAtlas} stitched together and uploaded.
	 */
	private static UploadedAtlas uploadedAtlas = UploadedAtlas.empty();

	static boolean isOutdated() {
		return isOutdated;
	}

	static UploadedAtlas getAtlas() {
		return uploadedAtlas;
	}

	/**
	 * Removes all textures from this atlas.
	 */
	@EventListener
	private static void reset(RefreshResourcesEvent event) {
		imageCache.clear();
		textures.clear();
		cumulatedArea = 0;
		minAtlasWidth = 1;
		isOutdated = true;
		uploadedAtlas = UploadedAtlas.empty();
	}

	/**
	 * Adds the given cutout to this atlas.
	 * If the current atlas did not already contain the cutout, it's marked as outdated.
	 */
	public static void register(TextureCutout cutout) {
		if (textures.containsKey(cutout))
			return;

		BufferedImage image = imageCache.computeIfAbsent(cutout.texture(), location -> Util.tryFatal(() -> {
			return TextureUtil.readBufferedImage(mc().getResourceManager().getResource(location).getInputStream());
		}));
		int maxX = Math.min(cutout.maxX(), image.getWidth());
		int maxY = Math.min(cutout.maxY(), image.getHeight());
		int width = maxX - cutout.minX();
		int height = maxY - cutout.minY();

		cumulatedArea += (width + PADDING) * (height + PADDING);
		minAtlasWidth = Math.max(minAtlasWidth, width);

		textures.put(cutout, new Texture(
			textures.size(),
			width, height,
			image.getRGB(cutout.minX(), cutout.minY(), width, height, null, 0, width)
		));

		isOutdated = true;
	}

	/**
	 * Packs all sprites into a single combined image using the FFDH Strip Packing algorithm.<br>
	 * The image is upload to GL in a new {@link UploadedAtlas}.
	 */
	public static void recompile() {
		int atlasWidth = Math.max(minAtlasWidth, (int) Math.ceil(Math.sqrt(cumulatedArea)));

		List<Texture> texturesDH = textures.values().stream()
			.sorted(Comparator.comparingInt(Texture::height).reversed())
			.toList();

		int[] slotX = new int[texturesDH.size()];
		int[] slotY = new int[texturesDH.size()];
		int[] rowX = new int[texturesDH.size() /* probably not used completely, but is the upper limit */];
		int[] rowY = new int[texturesDH.size() /* probably not used completely, but is the upper limit */];
		int rowCount = 0;
		int nextRowY = 0;

		// Distribute sprites
		for (Texture texture : texturesDH) {
			int row = 0;
			while (row < rowCount && rowX[row] + texture.width() > atlasWidth)
				row++;

			if (row == rowCount) {
				rowCount++;
				rowY[row] = nextRowY;
				nextRowY += texture.height() + PADDING;
			}

			slotX[texture.index] = rowX[row];
			slotY[texture.index] = rowY[row];
			rowX[row] += texture.width() + PADDING;
		}

		// Render sprites into the final atlas
		int atlasHeight = Math.max(nextRowY - PADDING, 1);
		BufferedImage atlas = new BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_INT_ARGB);
		UV[] slots = new UV[texturesDH.size()];

		for (Texture texture : texturesDH) {
			int minX = slotX[texture.index];
			int minY = slotY[texture.index];

			atlas.setRGB(minX, minY, texture.width(), texture.height(), texture.pixels(), 0, texture.width());
			slots[texture.index] = new UV(
				minX / (double) atlasWidth,
				(minX + texture.width()) / (double) atlasWidth,
				minY / (double) atlasHeight,
				(minY + texture.height()) / (double) atlasHeight
			);
		}

		int textureId = TextureUtil.glGenTextures();

		TextureUtil.uploadTextureImageAllocate(textureId, atlas, false /* blur */, false /* clamp uv */);
		uploadedAtlas.delete();
		uploadedAtlas = new UploadedAtlas(textureId, slots, ImmutableMap.copyOf(textures));
		isOutdated = false;
	}

	/**
	 * A single texture added to this atlas.
	 * @param index The texture's index in this atlas.
	 * @param width The texture's width in pixels.
	 * @param height The texture's height in pixels.
	 * @param pixels The RGBA pixels of the texture.
	 */
	record Texture(int index, int width, int height, int[] pixels) {}

	/**
	 * A stitched together atlas uploaded to GL.
	 */
	public static class UploadedAtlas {

		/**
		 * The GL id of the uploaded texture.
		 */
		private final int textureId;
		/**
		 * UV coordinates of each slot in this atlas.
		 */
		private final UV[] slots;
		/**
		 * All textures contained in this atlas.
		 */
		private final Map<TextureCutout, Texture> textures;

		UploadedAtlas(int textureId, UV[] slots, Map<TextureCutout, Texture> textures) {
			this.slots = slots;
			this.textures = textures;
			this.textureId = textureId;
		}

		/**
		 * Creates a new fallback atlas without any data.
		 */
		public static UploadedAtlas empty() {
			return new UploadedAtlas(0, new UV[0], ImmutableMap.of());
		}

		/**
		 * Deletes the texture uploaded to GL.
		 */
		void delete() {
			if (textureId != 0)
				TextureUtil.deleteTexture(textureId);
		}

		/**
		 * Binds the texture in GL.
		 */
		public void bind() {
			if (textureId != 0)
				GlStateManager.bindTexture(textureId);
		}

		/**
		 * @return The UV coordinates of the texture in this atlas.
		 */
		public UV getUV(TextureCutout cutout) {
			return slots[textures.get(cutout).index()];
		}
	}

}
