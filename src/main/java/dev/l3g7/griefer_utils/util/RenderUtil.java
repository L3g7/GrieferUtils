package dev.l3g7.griefer_utils.util;

import net.labymod.core.LabyModCore;
import net.labymod.core.WorldRendererAdapter;
import net.labymod.main.LabyMod;
import net.labymod.user.User;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.BlockPos;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3d;
import java.awt.*;

import static org.lwjgl.opengl.GL11.GL_LINES;

public class RenderUtil {

    public static String formatTime(long endTime, boolean shorten) {
        long secondsRaw = (endTime - System.currentTimeMillis()) / 1000L;
        if(secondsRaw <= 0L)
            return shorten ? "0s" : "0 Sekunden";
        return formatTime(secondsRaw / 60L / 60L, secondsRaw / 60L % 60L, secondsRaw % 60L, shorten);
    }

    public static String formatTime(long hours, long minutes, long seconds, boolean shorten) {
        String result = "";
        if (hours > 0L)
            result += shorten ? hours + "h " : hours == 1L ? "eine Stunde, " : hours + " Stunden, ";
        if (minutes > 0L)
            result += shorten ? minutes + "m " : minutes == 1L ? "eine Minute, " : minutes + " Minuten, ";
        result += shorten ? seconds + "s" : seconds == 1L ? "eine Sekunde" : seconds + " Sekunden";
        return result;
    }

    /**
     * Copied from LabyMod i think
     * TODO: there has to be an existing version in Minecraft
     */
    public static void renderSubOrSuperTitles(AbstractClientPlayer entity, double x, double y, double z, String subTitle, String superTitle) {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        boolean canRender = Minecraft.isGuiEnabled() && !entity.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer) && entity.riddenByEntity == null;
        if (canRenderName(entity) || entity == renderManager.livingPlayer && LabyMod.getSettings().showMyName && canRender) {
            float f = entity.isSneaking() ? 32.0f : 64.0f;
            double distance = entity.getDistanceSqToEntity(renderManager.livingPlayer);
            if (distance < f * f) {
                User user = LabyMod.getInstance().getUserManager().getUser(entity.getUniqueID());
                float maxNameTagHeight = user == null || !LabyMod.getSettings().cosmetics ? 0.0f : user.getMaxNameTagHeight();
                GlStateManager.alphaFunc(516, 0.1f);
                y += maxNameTagHeight;
                if (!entity.isSneaking()) {
                    if(subTitle != null) {
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(0.0d, -0.22d, 0.0d);
                        renderLivingLabelCustom(entity, subTitle, x, y, z);
                        GlStateManager.popMatrix();
                    }
                    if(superTitle != null) {
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(0.0d, -0.2d, 0.0d);
                        renderLivingLabelCustom(entity, superTitle, x, y + .32, z);
                        GlStateManager.popMatrix();
                    }
                }
            }
        }
    }

    /**
     * Copied from LabyMod i think
     * TODO: there has to be an existing version in Minecraft
     */
    private static void renderLivingLabelCustom(Entity entityIn, String str, double x, double y, double z) {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        double d0 = entityIn.getDistanceSqToEntity(renderManager.livingPlayer);
        if (d0 <= (double)(64 * 64)) {
            float fixedPlayerViewX = renderManager.playerViewX * Minecraft.getMinecraft().gameSettings.thirdPersonView == 2 ? -1f : 1f;
            FontRenderer fontrenderer = renderManager.getFontRenderer();
            float f1 = 0.016666668f * (float) 0.8;
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x, (float) y + entityIn.height + 0.5f, (float) z);
            GL11.glNormal3f(0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(fixedPlayerViewX, 1.0f, 0.0f, 0.0f);
            GlStateManager.scale(-f1, -f1, f1);
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRendererAdapter worldRenderer = LabyModCore.getWorldRenderer();
            int i = 0;
            int j = fontrenderer.getStringWidth(str) / 2;
            GlStateManager.disableTexture2D();
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            worldRenderer.pos(-j - 1, -1 + i, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
            worldRenderer.pos(-j - 1, 8 + i, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
            worldRenderer.pos(j + 1, 8 + i, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
            worldRenderer.pos(j + 1, -1 + i, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture2D();
            fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, 0x20FFFFFF);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, -1);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.popMatrix();
        }
    }

    /**
     * Copied from LabyMod i think
     * TODO: there has to be an existing version in Minecraft
     */
    public static boolean canRenderName(AbstractClientPlayer entity) {
        EntityPlayerSP entityplayersp = Minecraft.getMinecraft().thePlayer;

        if (entity != entityplayersp) {
            Team team = entity.getTeam();
            Team team1 = entityplayersp.getTeam();

            if (team != null) {
                switch (team.getNameTagVisibility()) {
                    case NEVER:
                        return false;
                    case HIDE_FOR_OTHER_TEAMS:
                        return team1 == null || team.isSameTeam(team1);
                    case HIDE_FOR_OWN_TEAM:
                        return team1 == null || !team.isSameTeam(team1);
                    default:
                        return true;
                }
            }
        }

        return Minecraft.isGuiEnabled() && entity != Minecraft.getMinecraft().getRenderManager().livingPlayer
                && !entity.isInvisibleToPlayer(entityplayersp) && entity.riddenByEntity == null;
    }

    public static void renderLine(BlockPos start, BlockPos end, Color color) {
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
