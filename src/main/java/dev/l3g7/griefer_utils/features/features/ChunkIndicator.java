package dev.l3g7.griefer_utils.features.features;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.ItemBuilder;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.KeySetting;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3d;
import java.awt.Color;

import static org.lwjgl.opengl.GL11.GL_LINES;

@Singleton
public class ChunkIndicator extends Feature {

	private static final Color BLUE = new Color(0x3F3FFF);
	private static final Color CYAN = new Color(0x009B9B);
	private static final int[] SIN = new int[]{0, 1, 0, -1};
	private static final int[] COS = new int[]{1, 0, -1, 0};

	private Color color;
	private BlockPos chunkRoot;
	private boolean enabled = false;

	private final BooleanSetting red_lines = new BooleanSetting()
			.name("Rote Linien")
			.config("features.chunk_indicator.red")
			.description("Ob die Linien in den Ecken der anliegenden Chunks angezeigt werden sollen.")
			.icon(new ItemBuilder(Blocks.wool).damage(14))
			.defaultValue(true);

	private final BooleanSetting yellow_lines = new BooleanSetting()
			.name("Gelbe Linien")
			.config("features.chunk_indicator.yellow")
			.description("Ob die 2x2-Linien angezeigt werden sollen.")
			.icon(new ItemBuilder(Blocks.wool).damage(4))
			.defaultValue(true);

	private final BooleanSetting cyan_lines = new BooleanSetting()
			.name("Türkise Linien")
			.config("features.chunk_indicator.cyan")
			.description("Ob die 8x8-Linien angezeigt werden sollen.")
			.icon(new ItemBuilder(Blocks.wool).damage(9))
			.defaultValue(true);

	private final BooleanSetting blue_lines = new BooleanSetting()
			.name("Blaue Linien")
			.config("features.chunk_indicator.blue")
			.description("Ob die 16x16-Linien angezeigt werden sollen.")
			.icon(new ItemBuilder(Blocks.wool).damage(11))
			.defaultValue(true);

	private final KeySetting toggle = new KeySetting()
			.name("Chunk-Indikator")
			.config("features.chunk_indicator.key")
			.icon("chunk_indicator")
			.description("Zeigt dir die Chunk-Grenzen an.")
			.callback(() -> enabled = !enabled)
			.settingsEnabled(true)
			.subSettingsWithHeader("Chunk-Indikator", red_lines, yellow_lines, cyan_lines, blue_lines);

	public ChunkIndicator() {
		super(Category.FEATURE);
	}

	@Override
	public SettingsElement getMainElement() {
		return toggle;
	}

	@SubscribeEvent
	public void onRender(RenderWorldLastEvent ignored) {
		if (!enabled || !isCategoryEnabled() || player() == null)
			return;

		chunkRoot = new BlockPos(player().chunkCoordX * 16, 0, player().chunkCoordZ * 16);

		/* -------------- *
		 | Vertical lines |
		 * -------------- */

		// Lines of the outer chunks
		if (red_lines.get()) {
			color = Color.RED;
			for (int i = 0; i < 3; i++)
				verticalLine(16 * i, -16);
		}

		// Lines of the main chunk
		for (int i = 0; i < 8; i++) {
			color = null;

			if (blue_lines.get() && i == 0) color = BLUE;
			else if (cyan_lines.get() && i % 4 == 0) color = CYAN;
			else if (yellow_lines.get()) color = Color.YELLOW;

			if (color != null)
				verticalLine(2 * i, 0);
		}

		/* ---------------- *
		 | Horizontal lines |
		 * ---------------- */
		for (int i = 0; i < 257; i += 2) {
			color = null;

			if (blue_lines.get() && i % 16 == 0) color = BLUE;
			else if (cyan_lines.get() && i % 8 == 0) color = CYAN;
			else if (yellow_lines.get()) color = Color.YELLOW;

			if (color != null)
				draw4Lines(chunkRoot.add(0, i, 0), chunkRoot.add(16, i, 0), color);
		}
	}

	private void verticalLine(int xOffset, int zOffset) {
		BlockPos pos = chunkRoot.add(xOffset, 0, zOffset);
		draw4Lines(pos, pos.add(0, 256, 0), color);
	}

	private void draw4Lines(BlockPos start, BlockPos end, Color color) {
		for (int i = 0; i < 4; i++)
			drawLine(rotate(start, i), rotate(end, i), color);
	}

	private BlockPos rotate(BlockPos point, int angle) {
		BlockPos center = chunkRoot.add(8, 0, 8);

		double x1 = point.getX() - center.getX();
		double z1 = point.getZ() - center.getZ();

		double x2 = x1 * COS[angle] - z1 * SIN[angle];
		double z2 = x1 * SIN[angle] + z1 * COS[angle];

		return new BlockPos(x2 + center.getX(), point.getY(), z2 + center.getZ());
	}

	/**
	 * Based on <a href="https://github.com/CCBlueX/LiquidBounce/blob/5419a2894b4665b7695d0443180275a70f13607a/src/main/java/net/ccbluex/liquidbounce/utils/render/RenderUtils.java#L82">LiquidBounce's RenderUtils#drawBlockBox</a>
	 */
	private static void drawLine(BlockPos start, BlockPos end, Color color) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer buf = tessellator.getWorldRenderer();
		Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
		float partialTicks = LabyMod.getInstance().getPartialTicks();
		Vector3d cameraPos = new Vector3d(entity.prevPosX + ((entity.posX - entity.prevPosX) * partialTicks), entity.prevPosY + ((entity.posY - entity.prevPosY) * partialTicks), entity.prevPosZ + ((entity.posZ - entity.prevPosZ) * partialTicks));
		double x1 = start.getX() - cameraPos.getX();
		double y1 = start.getY() - cameraPos.getY();
		double z1 = start.getZ() - cameraPos.getZ();
		double x2 = end.getX() - cameraPos.getX();
		double y2 = end.getY() - cameraPos.getY();
		double z2 = end.getZ() - cameraPos.getZ();

		float oldLineWidth = GL11.glGetFloat(GL11.GL_LINE_WIDTH);
		GL11.glLineWidth(1.5f);
		GlStateManager.disableTexture2D();

		buf.begin(GL_LINES, DefaultVertexFormats.POSITION);
		GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

		buf.pos(x1, y1, z1).endVertex();
		buf.pos(x2, y2, z2).endVertex();

		tessellator.draw();

		GL11.glLineWidth(oldLineWidth);
		GlStateManager.enableTexture2D();
	}
}
