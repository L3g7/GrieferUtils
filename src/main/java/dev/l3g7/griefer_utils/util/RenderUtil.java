package dev.l3g7.griefer_utils.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

public class RenderUtil {

	public static FontRenderer symbolFontRenderer = new FontRenderer(Minecraft.getMinecraft().gameSettings, new ResourceLocation("griefer_utils/font/ascii.png"), Minecraft.getMinecraft().renderEngine, false);

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

	static {
		((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(RenderUtil.symbolFontRenderer);
	}
}