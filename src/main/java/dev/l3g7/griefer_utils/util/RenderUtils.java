package dev.l3g7.griefer_utils.util;

import static org.lwjgl.opengl.GL11.GL_LINES;

import java.awt.Color;

import javax.vecmath.Vector3d;

import org.lwjgl.opengl.GL11;

import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;

public class RenderUtils {

	public static void renderStatic(BlockPos first, BlockPos second, Color color) {
		RenderUtils.renderStatic(first, second, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
	}

	public static void renderStatic(BlockPos first, BlockPos second, float red, float green, float blue, float alpha) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer buf = tessellator.getWorldRenderer();
		Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
		float partialTicks = LabyMod.getInstance().getPartialTicks();
		Vector3d cameraPos = new Vector3d(entity.prevPosX + ((entity.posX - entity.prevPosX) * partialTicks), entity.prevPosY + ((entity.posY - entity.prevPosY) * partialTicks), entity.prevPosZ + ((entity.posZ - entity.prevPosZ) * partialTicks));
		double x1 = first.getX() - cameraPos.getX();
		double y1 = first.getY() - cameraPos.getY();
		double z1 = first.getZ() - cameraPos.getZ();
		double x2 = second.getX() - cameraPos.getX();
		double y2 = second.getY() - cameraPos.getY();
		double z2 = second.getZ() - cameraPos.getZ();

		float oldLineWidth = GL11.glGetFloat(GL11.GL_LINE_WIDTH);
		GL11.glLineWidth(1.5f);
		GlStateManager.disableTexture2D();

		buf.begin(GL_LINES, DefaultVertexFormats.POSITION);
		GL11.glColor4f(red, green, blue, alpha);

		buf.pos(x1, y1, z1).endVertex();
		buf.pos(x2, y2, z2).endVertex();

		tessellator.draw();

		GL11.glLineWidth(oldLineWidth);
		GlStateManager.enableTexture2D();
	}
}