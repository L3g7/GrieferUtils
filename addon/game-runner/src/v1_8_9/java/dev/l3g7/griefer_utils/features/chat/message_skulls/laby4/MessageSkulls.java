/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.message_skulls.laby4;


import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.MessageEvent;
import dev.l3g7.griefer_utils.core.misc.NameCache;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.Feature;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.Style;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.util.bounds.Rectangle;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.*;
import static net.labymod.api.client.component.Component.empty;
import static net.labymod.api.client.component.Component.space;

@Singleton
@ExclusiveTo(LABY_4)
public class MessageSkulls extends Feature {

	private static final ArrayList<Pattern> PATTERNS = new ArrayList<>(MESSAGE_PATTERNS) {{
		remove(GLOBAL_CHAT_PATTERN);
		add(STATUS_PATTERN);
	}};

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Kopf vor Nachrichten")
		.description("Zeigt den Kopf des Autors vor Nachrichten an.")
		.icon("steve");

	@EventListener
	public void onMsgReceive(MessageEvent.MessageModifyEvent event) {
		for (Pattern pattern : PATTERNS) {
			Matcher matcher = pattern.matcher(event.original.getFormattedText());
			if (!matcher.matches())
				continue;

			String name = NameCache.ensureRealName(matcher.group("name").replaceAll("§.", ""));

			event.setMessage((IChatComponent) empty()
				.append(Component.icon(new HeadIcon(name), Style.empty(), 8))
				.append(space())
				.append((Component) event.message));
			return;
		}

	}

	private static class HeadIcon extends Icon {

		private final String name;

		protected HeadIcon(String name) {
			super(() -> null, "https://fuck you labymod and your filter implementation");
			this.name = name;
		}

		@Override
		public int getResolutionWidth() {
			return super.getResolutionWidth();
		}

		public void render(Stack stack, float x, float y, float width, float height, boolean hover, int color, Rectangle stencil) {
			NetworkPlayerInfo playerInfo = MinecraftUtil.mc().getNetHandler().getPlayerInfo(name);
			if (playerInfo == null)
				return;

			y -= 1;
			float alpha = color == -1 ? 1 : (color >> 24) / 255f;

			DrawUtils.bindTexture(playerInfo.getLocationSkin());
			DrawUtils.drawTexture(x, y, 32, 32, 32, 32, 8, 8, alpha); // First layer
			DrawUtils.drawTexture(x, y, 160, 32, 32, 32, 8, 8, alpha); // Second layer

			GlStateManager.disableBlend();
			GlStateManager.disableAlpha();
		}

	}

}