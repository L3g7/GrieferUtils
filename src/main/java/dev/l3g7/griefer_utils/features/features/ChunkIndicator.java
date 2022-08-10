package dev.l3g7.griefer_utils.features.features;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.ItemBuilder;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.RenderUtil;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

@Singleton
public class ChunkIndicator extends Feature {

	private static final Color BLUE = new Color(0x3F3FFF);
	private static final Color CYAN = new Color(0x009B9B);
	private static final int[] SIN = new int[] {0, 1, 0, -1};
	private static final int[] COS = new int[] {1, 0, -1, 0};

	private Color color;
	private BlockPos chunkRoot;

	private final BooleanSetting red_lines = new BooleanSetting()
	 .name("Rote Linien")
	 .config("features.chunk_indicator.red")
	 .icon(new ItemBuilder(Blocks.wool).damage(14))
	 .defaultValue(true);

	private final BooleanSetting yellow_lines = new BooleanSetting()
	 .name("Gelbe Linien")
	 .config("features.chunk_indicator.yellow")
	 .icon(new ItemBuilder(Blocks.wool).damage(4))
	 .defaultValue(true);

	private final BooleanSetting cyan_lines = new BooleanSetting()
	 .name("Türkise Linien")
	 .config("features.chunk_indicator.cyan")
	 .icon(new ItemBuilder(Blocks.wool).damage(9))
	 .defaultValue(true);

	private final BooleanSetting blue_lines = new BooleanSetting()
	 .name("Blaue Linien")
	 .config("features.chunk_indicator.blue")
	 .icon(new ItemBuilder(Blocks.wool).damage(11))
	 .defaultValue(true);

	private final BooleanSetting enabled = new BooleanSetting()
	 .name("Chunk-Indikator")
	 .config("features.chunk_indicator.active")
	 .icon("chunk_indicator")
	 .description("Zeigt dir die Chunk-Grenzen an.")
	 .defaultValue(false)
	 .subSettingsWithHeader("Chunk-Indikator", red_lines, yellow_lines, cyan_lines, blue_lines);

	public ChunkIndicator() {
		super(Category.FEATURE);
	}

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	@SubscribeEvent
	public void onRender(RenderWorldLastEvent ignored) {
		if (!isActive()
		 || player() == null)
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

			if      (blue_lines.get() && i == 0) color = BLUE;
			else if (cyan_lines.get() && i % 4 == 0) color = CYAN;
			else if (yellow_lines.get())         color = Color.YELLOW;

			if (color != null)
				verticalLine(2 * i, 0);
		}

		/* ---------------- *
		 | Horizontal lines |
		 * ---------------- */
		for (int i = 0; i < 257; i += 2) {
			color = null;

			if      (blue_lines.get() && i % 16 == 0) color = BLUE;
			else if (cyan_lines.get() && i % 8 == 0)  color = CYAN;
			else if (yellow_lines.get())              color = Color.YELLOW;

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
			RenderUtil.renderLine(rotate(start, i), rotate(end, i), color);
	}

	private BlockPos rotate(BlockPos point, int angle) {
		BlockPos center = chunkRoot.add(8, 0, 8);

		double x1 = point.getX() - center.getX();
		double z1 = point.getZ() - center.getZ();

		double x2 = x1 * COS[angle] - z1 * SIN[angle];
		double z2 = x1 * SIN[angle] + z1 * COS[angle];

		return new BlockPos(x2 + center.getX(), point.getY(), z2 + center.getZ());
	}
}
