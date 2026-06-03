package dev.l3g7.griefer_utils.core.misc.color_patterns;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;

public class ColorPattern {

	static final int BOLD = 0b01;
	static final int OBFUSCATED = 0b10;

	private final String rankPattern;
	private final String namePattern;
	private final int decorations;

	protected ColorPattern(String rankPattern, String namePattern, int decorations) {
		this.rankPattern = rankPattern;
		this.namePattern = namePattern;
		this.decorations = decorations;
	}

	protected ColorPattern(char color, boolean bold) {
		this(String.valueOf(color), String.valueOf(color), bold ? BOLD : 0);
	}

	protected ColorPattern(String pattern) {
		this(pattern, pattern, BOLD);
	}

	/**
	 * @param rank formatted
	 * @param name formatted
	 */
	public static ColorPattern from(String rank, String name) {
		return ColorPatternFinder.findPattern(rank, name);
	}

	public static ColorPattern from(Matcher matcher) {
		return ColorPatternFinder.findPattern(matcher.group("rank"), matcher.group("name"));
	}

	public List<IChatComponent> paintRank(String rank, boolean allowBold, ChatStyle baseStyle) {
		return Painter.paint(rank, rankPattern, decorations & ~(allowBold ? 0 : BOLD), baseStyle);
	}

	public List<IChatComponent> paintName(String name, boolean allowBold, ChatStyle baseStyle) {
		return Painter.paint(name, namePattern, decorations & ~(allowBold ? 0 : BOLD), baseStyle);
	}

	public IChatComponent paintName(String name, boolean allowBold) {
		IChatComponent base = new ChatComponentText("");
		List<IChatComponent> painted = paintName(name, allowBold, new ChatStyle());
		for (IChatComponent sibling : painted)
			base.appendSibling(sibling);

		return base;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ColorPattern that))
			return false;

		return decorations == that.decorations
			&& Objects.equals(rankPattern, that.rankPattern)
			&& Objects.equals(namePattern, that.namePattern);
	}

}
