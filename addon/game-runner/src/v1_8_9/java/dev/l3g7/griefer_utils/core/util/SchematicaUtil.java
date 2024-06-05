package dev.l3g7.griefer_utils.core.util;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.api.ISchematic;
import com.github.lunatrius.schematica.client.renderer.RenderSchematic;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

import java.io.File;

import static com.github.lunatrius.schematica.proxy.ClientProxy.schematic;

public class SchematicaUtil {

	public static final File MATERIAL_FILE = new File(Schematica.proxy.getDirectory("dumps"), "Schematica-materials.txt");

	public static WorldClient getWorld() {
		return schematic;
	}

	public static ISchematic getSchematic() {
		return schematic.getSchematic();
	}

	public static MovingObjectPosition getMovingObjectPosition() {
		return ClientProxy.movingObjectPosition;
	}

	public static BlockPos getPosition() {
		return schematic.position;
	}

	public static boolean dontRender() {
		return schematic == null || !schematic.isRendering;
	}

	public static boolean shouldLayerBeRendered(int y) {
		return !schematic.isRenderingLayer || schematic.renderingLayer + schematic.position.field_177960_b == y;
	}

	public static void refresh() {
		RenderSchematic.INSTANCE.refresh();
	}

}