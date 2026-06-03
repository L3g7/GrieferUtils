package dev.l3g7.griefer_utils.core.misc.color_patterns;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static dev.l3g7.griefer_utils.core.misc.color_patterns.ColorPattern.BOLD;
import static dev.l3g7.griefer_utils.core.misc.color_patterns.ColorPattern.OBFUSCATED;

class Painter {

	public static List<IChatComponent> paint(String text, String pattern, int decorations, ChatStyle baseStyle) {
		ChatStyle style = baseStyle
			.setBold((decorations & BOLD) != 0)
			.setObfuscated((decorations & OBFUSCATED) != 0);

		if (pattern.length() == 1)
			// Fixed pattern
			return Collections.singletonList(new ChatComponentText(text)
				.setChatStyle(style.setColor(color(pattern.charAt(0)))));

		// Repeating pattern
		List<IChatComponent> components = new ArrayList<>();

		char[] colors = pattern.toCharArray();
		char[] chars = text.toCharArray();
		for (int i = 0; i < chars.length;) {
			StringBuilder content = new StringBuilder();
			char color = colors[i % colors.length];
			while (i < chars.length && colors[i % colors.length] == color)
				// Merge chars with same color
				content.append(chars[i++]);

			components.add(new ChatComponentText(content.toString())
				.setChatStyle(style.createShallowCopy().setColor(color(color))));
		}

		return components;
	}

	private static EnumChatFormatting color(char code) {
		return EnumChatFormatting.func_175744_a(Integer.parseInt(String.valueOf(code), 16));
	}

}
