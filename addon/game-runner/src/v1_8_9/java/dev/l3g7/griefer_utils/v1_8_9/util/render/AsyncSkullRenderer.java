/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.util.render;

import dev.l3g7.griefer_utils.api.event.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.events.AccountSwitchEvent;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;

import java.util.concurrent.ConcurrentHashMap;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.name;

public class AsyncSkullRenderer {

	static {
		// Load class manually
		TileEntityRendererDispatcher.instance.getFontRenderer();
	}

	private static final ConcurrentSet<String> namesRequested = new ConcurrentSet<>();
	private static final ConcurrentHashMap<String, ItemStack> requestedHeads = new ConcurrentHashMap<>();

	public static void renderPlayerSkull(int x, int y) {
		String token = mc().getSession().getToken();
		renderSkull(x, y, token.equals("FML") || token.equals("0") ? "GrieferUtils" : name());
	}

	public synchronized static void renderSkull(int x, int y, String name) {
		ItemStack stack = requestedHeads.get(name);
		if (stack == null) {
			requestSkull(name);
			return;
		}

		GlStateManager.pushMatrix();

		GlStateManager.translate(x + 15.3333, y + 13, 0);
		GlStateManager.scale(10, 10, -10);
		GlStateManager.rotate(210, 1, 0, 0);
		GlStateManager.rotate(-135, 0, 1, 0);

		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.enableDepth();
		TileEntityItemStackRenderer.instance.renderByItem(stack);

		GlStateManager.popMatrix();
	}

	private static synchronized void requestSkull(String name) {
		if (namesRequested.add(name))
			new Thread(() -> requestedHeads.put(name, ItemUtil.fromNBT("{id:\"minecraft:skull\",Count:1b,tag:{SkullOwner:\"" + name + "\"},Damage:3s}"))).start();
	}

	@OnEnable
	private static void preloadDefaultSkulls() {
		requestSkull("GrieferInfo");
		onAccountSwitch(null);
	}

	@EventListener
	private static void onAccountSwitch(AccountSwitchEvent event) {
		if (mc().getSession() != null && mc().getSession().getToken() != null)
			requestSkull(mc().getSession().getToken().equals("FML") ? "GrieferUtils" : name());
	}

}
