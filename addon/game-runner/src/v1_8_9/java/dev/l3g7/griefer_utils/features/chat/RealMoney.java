/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.Priority;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageModifyEvent;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.StringSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.init.Items;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.ListIterator;
import java.util.regex.Matcher;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;

@Singleton
public class RealMoney extends Feature {

	private final StringSetting tag = StringSetting.create()
		.name("Tag")
		.description("Womit eingehenden Bezahlungen markiert werden sollen.")
		.icon("XZRF:name_tag")
		.defaultValue("&a [✔]");

	private final DropDownSetting<TagPosition> position = DropDownSetting.create(TagPosition.class)
		.name("Position")
		.description("Ob der Tag an den Anfang oder das Ende der Nachricht angehängt wird.")
		.icon("XZRF:wooden_board")
		.defaultValue(TagPosition.AFTER);

	private final SwitchSetting highlightCents = SwitchSetting.create()
		.name("Cent-Beträge kennzeichnen")
		.description("Markiert Cent-Beträge rot.")
		.icon("XZRF:color_palette")
		.defaultValue(true);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name(LABY_3.isActive()
			? "Echtgeld- Erkennung" // Spacing to allow word wrap
			: "Echtgeld-Erkennung")
		.description("Fügt einen Tag zu eingehenden Bezahlungen hinzu.")
		.icon("XZRF:coin")
		.subSettings(highlightCents, tag, position);

	@EventListener(priority = Priority.LOW)
	public void onMessageReceive(MessageModifyEvent event) {
		Matcher matcher = Constants.PAYMENT_RECEIVE_PATTERN.matcher(event.original.getFormattedText());
		if (!matcher.matches())
			return;

		if (highlightCents.get())
			markCentsRed(event.message, matcher.group("amount"));

		String text = "§r" + tag.get().replace('&', '§') + "§r";

		if (position.get() == TagPosition.BEFORE)
			event.setMessage(new ChatComponentText(text).appendSibling(event.message));
		else
			event.setMessage(event.message.appendText(text));
	}

	private static void markCentsRed(IChatComponent icc, String amountString) {
		int dotIndex = amountString.indexOf('.');
		if (dotIndex == -1)
			return;

		ListIterator<IChatComponent> it = icc.getSiblings().listIterator(icc.getSiblings().size());
		while (it.hasPrevious()) {
			IChatComponent sibling = it.previous();
			if (!(sibling instanceof ChatComponentText))
				continue;

			String text = sibling.getUnformattedTextForChat();
			int index = text.indexOf(amountString);
			if (index == -1)
				continue;

			it.next();
			String preText = text.substring(0, index) + amountString.substring(0, dotIndex);
			String postText = text.substring(index + amountString.length());

			Reflection.set(sibling, "text", preText);

			IChatComponent cents = sibling.createCopy();
			cents.getChatStyle().setColor(EnumChatFormatting.RED);
			Reflection.set(cents, "text", amountString.substring(dotIndex));
			it.add(cents);

			IChatComponent post = sibling.createCopy();
			Reflection.set(post, "text", postText);
			it.add(post);
			return;
		}
	}

	private enum TagPosition implements Named {

		BEFORE("Davor"), AFTER("Danach");

		private final String name;
		TagPosition(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

	}

}
