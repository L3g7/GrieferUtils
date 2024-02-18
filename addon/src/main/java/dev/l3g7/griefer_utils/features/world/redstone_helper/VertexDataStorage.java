package dev.l3g7.griefer_utils.features.world.redstone_helper;

import dev.l3g7.griefer_utils.core.misc.functions.Predicate;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

public class VertexDataStorage { // TODO: Split between texture data and vertex data (calculate vertex data in RenderObjects?)

	private static final String CHARS = "0123456789⬆";
	private static final String[] COMBINED_STRINGS = new String[] {"10", "11", "12", "13", "14", "15"};

	@SuppressWarnings("UnnecessaryUnicodeEscape") // (was just copied from Minecraft's FontRenderer)
	private static final String CHARS_IN_ASCII = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000";

	public static final double[][][] vertexOffsets = new double[16 + 1 + 1][][]; // data -> rotations -> 8 / 16 offsets
	private static final int[][] rawTextureData = new int[CHARS.length()][]; // letters -> {startX, endX, startY, endY} (once or twice)
	private static final double[][] scaledTextureData = new double[16 + 1 + 1][];

	private static final Map<Integer, CharClamper> clampers = new HashMap<>();

	private static int getIndex(Object obj) {
		String string = String.valueOf(obj);

		if (string.length() == 1)
			return CHARS.indexOf(string.charAt(0));

		for (int i = 0; i < COMBINED_STRINGS.length; i++)
			if (COMBINED_STRINGS[i].equals(string))
				return CHARS.length() + i;

		throw new IllegalArgumentException("No data exsists for string \"" + string + "\"");
	}

	public static double[] getTexData(Object obj) {
		return scaledTextureData[getIndex(obj)];
	}

	public static double[][] getVertexOffsets(Object obj) {
		return vertexOffsets[getIndex(obj)];
	}

	private static void calculateData()  { // TODO: Remove spaghetti
		for (int i = 0; i < CHARS.length(); i++) {
			char c = CHARS.charAt(i);
			int textureIndex = CHARS_IN_ASCII.indexOf(c);
			if (textureIndex != -1) {
				CharClamper clamper = clampers.computeIfAbsent(-1, k -> CharClamper.newCharClamper("textures/font/ascii"));
				parseChar(clamper, i, textureIndex, 16, 2);
				continue;
			}

			textureIndex = c & 0xFF;
			int page = c >> 8;
			CharClamper clamper = clampers.computeIfAbsent(page, k -> CharClamper.newCharClamper(String.format("textures/font/unicode_page_%x", page)));
			parseChar(clamper, i, textureIndex, 4, 3);
		}

		for (int i = 0; i < COMBINED_STRINGS.length; i++) {
			String string = COMBINED_STRINGS[i];
			int firstCharIndex = CHARS.indexOf(string.charAt(0));
			int secondCharIndex = CHARS.indexOf(string.charAt(1));
			int arrayIndex = i + CHARS.length();

			double[] firstVertices = vertexOffsets[firstCharIndex][0];
			int firstWidth = rawTextureData[firstCharIndex][1] - rawTextureData[firstCharIndex][0];

			double[] secondVertices = vertexOffsets[secondCharIndex][0];
			int secondWidth = rawTextureData[secondCharIndex][1] - rawTextureData[secondCharIndex][0];

			vertexOffsets[arrayIndex] = VertexCalculator.combineVertices(firstVertices, firstWidth, secondVertices, secondWidth);

			// Combine texture data
			double[] texData = scaledTextureData[arrayIndex] = new double[8];
			System.arraycopy(scaledTextureData[firstCharIndex], 0, texData, 0, 4);
			System.arraycopy(scaledTextureData[secondCharIndex], 0, texData, 4, 4);
		}

//		double[] singleTexData = scaledTextureData[16];
//		double[] texData = new double[singleTexData.length * 4];
//		for (int i = 0; i < 4; i++) {
//			System.arraycopy(singleTexData, 0, texData, i * 4, 4);
//		}
	}

	private static void parseChar(CharClamper clamper, int i, int textureIndex, int rotations, double size) {
		rawTextureData[i] = clamper.clamp(textureIndex);
		int w = rawTextureData[i][1] - rawTextureData[i][0];
		int h = rawTextureData[i][3] - rawTextureData[i][2];
		vertexOffsets[i] = VertexCalculator.getVertices(w, h, rotations, size);

		double[] scaledTexData = scaledTextureData[i] = new double[4];
		scaledTexData[0] = rawTextureData[i][0] / (double) clamper.width;
		scaledTexData[1] = rawTextureData[i][1] / (double) clamper.width;
		scaledTexData[2] = rawTextureData[i][2] / (double) clamper.height;
		scaledTexData[3] = rawTextureData[i][3] / (double) clamper.height;
	}

	static {
		((IReloadableResourceManager) mc().getResourceManager()).registerReloadListener(resourceManager -> calculateData());
	}

	private static class CharClamper {

		private final int[] pixels;
		private final int width;
		private final int height;

		private final int charWidth;
		private final int charHeight;

		private int x, y;

		private static CharClamper newCharClamper(String path) {
			try {
				InputStream stream = mc().getResourceManager().getResource(new ResourceLocation(path + ".png")).getInputStream();
				return new CharClamper(TextureUtil.readBufferedImage(stream));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		private CharClamper(BufferedImage img) {
			width = img.getWidth();
			height = img.getHeight();
			charHeight = height / 16;
			charWidth = width / 16;

			pixels = new int[width * height];
			img.getRGB(0, 0, width, height, pixels, 0, width);
		}

		private int[] clamp(int index) {
			this.x = (index % 16) * charWidth;
			this.y = (index / 16) * charHeight;

			return new int[]{
				x + clamp(charWidth, false, this::isColumnEmpty),
				x + clamp(charWidth, true, this::isColumnEmpty),
				y + clamp(charHeight, false, this::isLineEmpty),
				y + clamp(charHeight, true, this::isLineEmpty)
			};
		}

		private int clamp(int end, boolean reversed, Predicate<Integer> check) {
			if (!reversed)
				for (int i = 0; i < end; i++)
					if (!check.test(i))
						return i;

			for (int i = end - 1; i >= 0; i--)
				if (!check.test(i))
					return i + 1;

			throw new IllegalStateException(String.format("char at %d %d is invisible!", x, y));
		}

		private boolean isColumnEmpty(int x) {
			for (int i = 0; i < charHeight; i++)
				if (!isPixelEmpty(this.x + x, y + i))
					return false;

			return true;
		}

		private boolean isLineEmpty(int y) {
			for (int i = 0; i < charWidth; i++)
				if (!isPixelEmpty(x + i, this.y + y))
					return false;

			return true;
		}

		private boolean isPixelEmpty(int x, int y) {
			return (pixels[x + y * width] >> 24) == 0;
		}

	}

	private static class VertexCalculator {

		private static final double spaceWidth = 1 / 64d;

		private static double[][] getVertices(double width, double height, int rotations, double size) {
			size = 128 / size;
			width /= size;
			height /= size;

			double[][] result = new double[rotations][];
			result[0] = new double[]{
				-width, width, width, -width,
				-height, -height, height, height
			};

			return doRotations(result, rotations);
		}

		private static double[][] combineVertices(double[] firstVertices, int firstWidth, double[] secondVertices, int secondWidth) {
			double[][] result = new double[16][];

			double[] origin = result[0] = new double[16];

			System.arraycopy(firstVertices, 0, origin, 0, 8);
			System.arraycopy(secondVertices, 0, origin, 8, 8);

			for (int i = 0; i < 4; i++)
				origin[i] = origin[i] + spaceWidth + secondWidth / 64d;

			for (int i = 8; i < 12; i++)
				origin[i] = origin[i] - spaceWidth - firstWidth / 64d;

			for (int i = 1; i < 16; i++) {
				double rotation = Math.toRadians(i * 22.5);
				result[i] = new double[16];

				for (int j = 0; j < 4; j++)
					rotate(result[0], result[i], j, rotation);
			}

			return doRotations(result, 16);
		}

		private static double[][] doRotations(double[][] array, int rotations) {
			double singleRotation = 360 / (double) rotations;

			for (int i = 1; i < rotations; i++) {
				double rotation = Math.toRadians(i * singleRotation);
				array[i] = new double[array[0].length];

				for (int j = 0; j < 4; j++)
					rotate(array[0], array[i], j, rotation);

				if (array[i].length > 8)
					for (int j = 8; j < 12; j++)
						rotate(array[0], array[i], j, rotation);
			}

			return array;
		}

		private static void rotate(double[] original, double[] result, int index, double rotation) {
			double x = original[index];
			double y = original[index + 4];

			double dlen = Math.sqrt(x * x + y * y);
			x /= dlen;
			y /= dlen;

			double yaw = Math.atan2(y, x);

			yaw += rotation;

			result[index] = dlen * Math.cos(yaw);
			result[index + 4] = dlen * Math.sin(yaw);
		}

	}

}