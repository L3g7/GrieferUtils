/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Function;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageSendEvent;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.suggest;

/**
 * A utility class for methods related to {@link IChatComponent}s.
 */
public class IChatComponentUtil {

	private static final String COMMAND = "/grieferutils_namehistory_prompt_fix ";

	@EventListener
	private static void onMessageSend(MessageSendEvent event) {
		if (event.message.startsWith(COMMAND)) {
			suggest(event.message.substring(COMMAND.length()));
			event.cancel();
		}
	}

	public static void setNameWithPrefix(IChatComponent iChatComponent, String name, String realName, String prefix, boolean isTabList) {
		List<IChatComponent> everything = iChatComponent.getSiblings();
		IChatComponent parent = everything.size() > 0 ? everything.get(everything.size() - 1) : iChatComponent;

		if (parent.getSiblings().isEmpty())
			parent = iChatComponent;

		List<IChatComponent> lastSiblings = parent.getSiblings();

		int playerIndex = -1;

		for (ListIterator<IChatComponent> iterator = lastSiblings.listIterator(); iterator.hasNext(); ) {
			String text = iterator.next().getUnformattedTextForChat();

			if (playerIndex == -1) {
				if (text.matches(" ?\u2503 "))
					playerIndex = iterator.nextIndex();
				continue;
			}

			if (text.contains(" ") || text.contains("]")) {
				if (isTabList)
					iterator.remove();
				break;
			} else {
				iterator.remove();
			}
		}

		if (playerIndex == -1) {
			// TODO: wtf
			return;
		}

		Collection<IChatComponent> nameComponents = paint(realName, prefix, isTabList);

		ChatComponentText nickName = new ChatComponentText("");
		paint(name, prefix, false).forEach(nickName::appendSibling);

		// Add the HoverEvent and make it italic
		ClickEvent clickEvent = parent.getChatStyle().getChatClickEvent();
		boolean withHover = !realName.equals(name);

		for (IChatComponent component : nameComponents) {
			if (withHover)
				component.getChatStyle()
					.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, nickName))
					.setItalic(true);

			if (clickEvent != null)
				component.getChatStyle().setChatClickEvent(withHover ? new ClickEvent(ClickEvent.Action.RUN_COMMAND, COMMAND + clickEvent.getValue()) : clickEvent);
		}

		parent.getChatStyle().setChatClickEvent(clickEvent);
		lastSiblings.addAll(playerIndex, nameComponents);
	}

	/**
	 * Paints the text in the given color pattern.
	 */
	public static Collection<IChatComponent> paint(String text, String colorPattern, boolean disableBold) {
		if (text == null)
			return ImmutableList.of(new ChatComponentText("404").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)));

		// Fixed-color patterns
		if (colorPattern.length() <= 2) {
			ChatStyle style = new ChatStyle().setColor(color(colorPattern.charAt(0)))
				.setBold(colorPattern.contains("l") && !disableBold)
				.setObfuscated(colorPattern.contains("k"));

			return ImmutableList.of(new ChatComponentText(text).setChatStyle(style));
		}

		// Repeating patterns
		List<IChatComponent> components = new ArrayList<>();
		char[] colors = colorPattern.toCharArray();

		for (int i = 0; i < text.length(); i++) {
			ChatStyle style = new ChatStyle()
				.setColor(color(colors[i % colors.length]))
				.setBold(!disableBold);

			String content = String.valueOf(text.charAt(i));
			if (colors[i % colors.length] == colors[(i + 1) % colors.length] && text.length() != i + 1)
				// Merge components with same color
				content += text.charAt(++i);

			components.add(new ChatComponentText(content).setChatStyle(style));
		}

		return components;
	}

	private static EnumChatFormatting color(char code) {
		return EnumChatFormatting.func_175744_a(Integer.parseInt(String.valueOf(code), 16));
	}

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
	 * Returns a substring of the formatted text, indexed using the unformatted text.
	 */
	public static String getFormattedSubstringByUnformattedRange(IChatComponent root, int start, int end) {
		StringBuilder text = new StringBuilder();

		int index = 0;
		for (MutableComponent component : getNestedSiblings(root)) {
			int len = component.getText().length();

			if (index >= start) {
				// Range already started
				if (index + len < end) {
					// Range end after component
					text.append(component.getFormattedSubstring(0, component.getText().length()));
				} else {
					// Range end in component
					text.append(component.getFormattedSubstring(0, end - index));
				}
			} else if (index + len >= start) {
				// Range start in component
				if (index + len < end) {
					// Range end after component
					text.append(component.getFormattedSubstring(start - index, component.getText().length()));
				} else {
					// Range start and end in component
					return component.getFormattedSubstring(start - index, end - index);
				}
			}

			index += len;
			if (index >= end)
				return text.toString();
		}

		return text.toString();
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

		private String getFormattedSubstring(int start, int end) {
			return component.getChatStyle().getFormattingCode()
				+ getText().substring(start, end)
				+ EnumChatFormatting.RESET;
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
