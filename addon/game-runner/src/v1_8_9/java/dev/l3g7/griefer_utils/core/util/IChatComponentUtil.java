/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util;

import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Function;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.util.EnumChatFormatting.RESET;

/**
 * A utility class for methods related to {@link IChatComponent}s.
 */
public class IChatComponentUtil {

	/**
	 * Converts an index pointing to an unformatted char to an index to the same char in formatted text.
	 */
	public static int unformattedIndexToFormatted(IChatComponent root, int index) {
		if (index <= 0)
			return 0;

		String formatted = root.getFormattedText();
		int rawIndex = 0;

		char[] chars = formatted.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (c == '§') {
				// Skip next
				i++;
			} else {
				rawIndex++;
				if (rawIndex == index)
					return i + 1;
			}
		}

		return chars.length;
	}

	/**
	 * Converts an index pointing to a formatted char to an index to the same char in unformatted text.
	 */
	public static int formattedIndexToUnformatted(IChatComponent root, int index) {
		String formatted = root.getFormattedText();
		int rawIndex = 0;

		char[] chars = formatted.toCharArray();
		for (int i = 0; i < index; i++) {
			char c = chars[i];
			if (c == '§')
				// Skip next
				i++;
			else
				rawIndex++;
		}

		return rawIndex;
	}

	/**
	 * Builds a copy of the text, truncated from start to end.
	 *
	 * @param start inclusive, formatted
	 * @param end   exclusive, formatted
	 */
	public static IChatComponent extractFromFormatted(IChatComponent root, int start, int end) {
		start = formattedIndexToUnformatted(root, start);
		end = formattedIndexToUnformatted(root, end);
		return extractFromUnformatted(root, start, end);
	}

	/**
	 * Builds a copy of the text, truncated from start to end.
	 *
	 * @param start inclusive, unformatted
	 * @param end   exclusive, unformatted
	 */
	public static IChatComponent extractFromUnformatted(IChatComponent root, int start, int end) {
		IChatComponent res = root.createCopy();
		replace(res, end, res.getUnformattedText().length(), Collections.emptyList()); // Remove end
		replace(res, 0, start, Collections.emptyList()); // Remove start
		return res;
	}

	/**
	 * Replaces every "target" capture group matched by the pattern with the replacement.
	 */
	public static void replace(IChatComponent root, Pattern pattern, String target, Function<Matcher, List<IChatComponent>> replacement) {
		Matcher matcher = pattern.matcher(root.getFormattedText());
		while (matcher.find()) {
			if (matcher.group(target) == null)
				continue;

			int s = formattedIndexToUnformatted(root, matcher.start(target));
			int e = formattedIndexToUnformatted(root, matcher.end(target));
			List<IChatComponent> newComponents = replacement.apply(matcher);
			if (newComponents != null)
				replace(root, s, e, newComponents);
		}
	}

	/**
	 * Removes all text from start to end, removing or in-place truncating all affected components, and
	 * inserts the replacement.
	 *
	 * @param start inclusive, unformatted
	 * @param end   exclusive, unformatted
	 */
	public static void replace(IChatComponent root, int start, int end, List<IChatComponent> replacement) {
		int index = 0;
		for (MutableComponent component : getNestedSiblings(root)) {
			int len = component.getText().length();

			if (index > start) {
				// Replacement already started
				if (index + len < end) {
					// Range end after component, completely removed
					component.remove();
				} else {
					// Range end in component, text start removed
					component.setText(component.getText().substring(end - index));
				}
			} else if (index + len > start) {
				// Range start in component
				if (index + len < end) {
					// Range end after component, text end removed
					component.setText(component.getText().substring(0, start - index));

					// Insert replacement after element containing range start
					component.append(replacement);
				} else {
					// Range start and end in component, split
					MutableComponent prevComponent = component.split();
					prevComponent.setText(prevComponent.getText().substring(0, start - index));
					component.setText(component.getText().substring(end - index));

					// Insert replacement after element containing range start
					prevComponent.append(replacement);
				}
			}

			index += len;
			if (index >= end)
				return;
		}
	}

	/**
	 * Recursively collects siblings depth-first.
	 */
	public static List<MutableComponent> getNestedSiblings(IChatComponent component) {
		List<MutableComponent> list = new ArrayList<>();
		addSiblingsRecursive(null, component, list);
		return list;
	}

	private static void addSiblingsRecursive(IChatComponent parent, IChatComponent component, List<MutableComponent> destination) {
		destination.add(new MutableComponent(parent, component));
		for (IChatComponent sibling : component.getSiblings())
			addSiblingsRecursive(component, sibling, destination);
	}

	/**
	 * Wrapper for in-place mutations of IChatComponents.
	 */
	public static final class MutableComponent {
		private final IChatComponent parent;
		private IChatComponent component;

		private MutableComponent(IChatComponent parent, IChatComponent component) {
			this.parent = parent;
			this.component = component;
		}

		public String getText() {
			return component.getUnformattedTextForChat();
		}

		public String getFormattedSubstring(int start, int end) {
			return component.getChatStyle().getFormattingCode()
				+ getText().substring(start, end)
				+ RESET;
		}

		public void setText(String newText) {
			// Keep even if text is empty, as component might be used to index append()

			IChatComponent newComponent = new ChatComponentText(newText);
			newComponent.setChatStyle(component.getChatStyle());
			for (IChatComponent sibling : component.getSiblings())
				newComponent.appendSibling(sibling);

			set(newComponent);
		}

		public void set(IChatComponent newComponent) {
			List<IChatComponent> siblings = parent.getSiblings();
			siblings.set(indexOf(component), newComponent);
			newComponent.getChatStyle().setParentStyle(parent.getChatStyle());
			component = newComponent;
		}

		public void remove() {
			parent.getSiblings().remove(indexOf(component));
		}

		/**
		 * Appends the components immediately after this component.
		 */
		public void append(List<IChatComponent> components) {
			if (components.isEmpty())
				return;

			List<IChatComponent> siblings = parent.getSiblings();
			siblings.addAll(indexOf(component) + 1, components);
			for (IChatComponent component : components)
				component.getChatStyle().setParentStyle(parent.getChatStyle());
		}

		/**
		 * Splits this component into two components with the same content.
		 *
		 * @return the new component (first)
		 */
		public MutableComponent split() {
			MutableComponent copy = new MutableComponent(parent, component.createCopy());
			copy.component.getSiblings().clear();

			List<IChatComponent> siblings = parent.getSiblings();
			siblings.add(indexOf(component), copy.component);
			return copy;
		}

		/**
		 * Siblings indexOf using identity equals instead of Object#equals.
		 */
		private int indexOf(IChatComponent component) {
			List<IChatComponent> siblings = parent.getSiblings();
			for (int i = 0; i < siblings.size(); i++)
				if (siblings.get(i) == component)
					return i;

			return -1;
		}

		@Override
		public String toString() {
			return component.toString();
		}

	}

}
