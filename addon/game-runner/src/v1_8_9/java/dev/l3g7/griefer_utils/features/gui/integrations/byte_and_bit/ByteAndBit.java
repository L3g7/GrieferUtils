/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * This file has been modified by itzW0lf.
 */

package dev.l3g7.griefer_utils.features.gui.integrations.byte_and_bit;


import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.events.StaticDataReceiveEvent;
import dev.l3g7.griefer_utils.core.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.events.render.RenderWorldLastEvent;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.settings.types.KeySetting;
import dev.l3g7.griefer_utils.core.settings.types.SliderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import net.minecraft.util.AxisAlignedBB;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.gui.integrations.byte_and_bit.data.BABBot;
import dev.l3g7.griefer_utils.features.gui.integrations.byte_and_bit.data.BotSource;
import dev.l3g7.griefer_utils.features.gui.integrations.byte_and_bit.gui.BotshopGUI;
import dev.l3g7.griefer_utils.labymod.laby3.settings.types.KeySettingImpl;
import net.labymod.api.client.gui.screen.key.Key;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Vec3;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import org.lwjgl.input.Keyboard;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

/**
 * Implements any <a href="byteandbitstudio.de">ByteAndBit-Studio</a> features
 * This currently only includes functionality regarding bot "menus".
 */
@Singleton
public class ByteAndBit extends Feature {

	protected static final Map<String, BABBot> allBots = new ConcurrentHashMap<>();
	Map<String, BABBot> renderedBots = new ConcurrentHashMap<>();
	private final Map<String, Long> lastScanAttempt = new ConcurrentHashMap<>();
	private static final long SCAN_RETRY_INTERVAL = 5000;

	private final SliderSetting nearbyRadius = SliderSetting.create()
		.name("Erkennungsradius (Blöcke)")
		.description("Radius außerhalb der Botkammer, in dem das Botshop-Gui angezeigt und geöffnet werden kann.")
		.icon("cog")
		.since("2.5.0")
		.min(10).max(50)
		.defaultValue(10);

	private final SwitchSetting saveZones = SwitchSetting.create()
		.name("Botkammern speichern")
		.description("Speichert die Koordinaten bekannter Botkammern, damit sie beim nächsten Login sofort verfügbar sind.")
		.icon("floppy_disk")
		.since("2.5.0")
		.defaultValue(false);

	@MainElement
	private final KeySetting keybind = KeySetting.create()
		.name("Botshop-Gui")
		.description("Öffnet das Botshop-Gui von unterstützten BotShops.")
		.icon("high_res/byte_and_bit")
		.defaultValue(Keyboard.KEY_RETURN)
		.pressCallback(this::onKeyPress)
		.subSettings(nearbyRadius, saveZones);

	@EventListener
	private void onStaticData(StaticDataReceiveEvent event) {
		this.syncBots(event.data.botSources);
		TickScheduler.runAfterClientTicks(() -> syncBots(event.data.botSources), 60 * 20 * 5);
	}

	private void syncBots(BotSource[] botSources) {
		for (BotSource botSource : botSources)
			syncBotSource(botSource);
	}

	private void syncBotSource(BotSource botSource) {
		botSource.get().thenAccept(bots -> {
			if (bots.isEmpty()) {
				TickScheduler.runAfterClientTicks(() -> syncBotSource(botSource), 30 * 20);
				return;
			}
			for (String bot : bots) {
				BABBot babBot = new BABBot(botSource.getUrl(), bot);
				allBots.put(bot, babBot);
				if (saveZones.get())
					loadZone(bot, babBot);
				babBot.sync().whenComplete((dataPresent, ex) -> {
					if (Boolean.TRUE.equals(dataPresent)) {
						renderedBots.put(bot, babBot);
						if (saveZones.get())
							persistZone(bot, babBot.botZone);
					}
				});
			}
		});
	}

	private static final String ZONE_CONFIG_PREFIX = "gui.byte_and_bit.zones.";

	private void loadZone(String uuid, BABBot bot) {
		String base = ZONE_CONFIG_PREFIX + uuid;
		if (!Config.has(base + ".x1")) return;
		bot.botZone = AxisAlignedBB.fromBounds(
			Config.get(base + ".x1").getAsDouble(),
			Config.get(base + ".y1").getAsDouble(),
			Config.get(base + ".z1").getAsDouble(),
			Config.get(base + ".x2").getAsDouble(),
			Config.get(base + ".y2").getAsDouble(),
			Config.get(base + ".z2").getAsDouble()
		);
		renderedBots.put(uuid, bot);
	}

	private void persistZone(String uuid, AxisAlignedBB zone) {
		if (zone == null) return;
		String base = ZONE_CONFIG_PREFIX + uuid;
		Config.set(base + ".x1", new JsonPrimitive(zone.minX));
		Config.set(base + ".y1", new JsonPrimitive(zone.minY));
		Config.set(base + ".z1", new JsonPrimitive(zone.minZ));
		Config.set(base + ".x2", new JsonPrimitive(zone.maxX));
		Config.set(base + ".y2", new JsonPrimitive(zone.maxY));
		Config.set(base + ".z2", new JsonPrimitive(zone.maxZ));
		Config.save();
	}

	@EventListener(triggerWhenDisabled = true)
	public void onCBLeave(CitybuildJoinEvent event) {
		for (BABBot value : allBots.values()) {
			value.invalidateCache();
		}
	}

	@EventListener(triggerWhenDisabled = true)
	public void onNewPlayerDetect(PacketReceiveEvent<S0CPacketSpawnPlayer> p) {
		String shortUuid = p.packet.getPlayer().toString().replaceAll("-", "");
		BABBot bot = allBots.get(shortUuid);
		if (bot == null) return;
		bot.sync().whenComplete((dataPresent, ex) -> {
			if (ex != null) {
				ex.printStackTrace();
				return;
			}
			if (!dataPresent) return;
			renderedBots.put(shortUuid, bot);
		});
	}

	@EventListener
	public void onEntityYeet(PacketReceiveEvent<S13PacketDestroyEntities> p) {
		if (world() == null || player() == null)
			return;

		for (int id : p.packet.getEntityIDs()) {
			Entity e = world().getEntityByID(id);
			if (!(e instanceof EntityPlayer player)) continue;

			String uuid = player.getUniqueID().toString().replaceAll("-", "");
			if (!renderedBots.containsKey(uuid)) continue;

			renderedBots.get(uuid).invalidateCache();
			renderedBots.remove(uuid);
		}
	}

	@EventListener
	public void onRenderTick(RenderWorldLastEvent e) {
		if (world() != null && player() != null) {
			long now = System.currentTimeMillis();
			for (EntityPlayer entity : world().playerEntities) {
				String uuid = entity.getUniqueID().toString().replaceAll("-", "");
				BABBot bot = allBots.get(uuid);
				if (bot == null || renderedBots.containsKey(uuid))
					continue;
				Long lastAttempt = lastScanAttempt.get(uuid);
				if (lastAttempt != null && now - lastAttempt < SCAN_RETRY_INTERVAL)
					continue;
				lastScanAttempt.put(uuid, now);
				bot.sync().whenComplete((dataPresent, ex) -> {
					if (Boolean.TRUE.equals(dataPresent))
						renderedBots.put(uuid, bot);
				});
			}
		}

		for (BABBot bot : renderedBots.values()) {
			if (bot.botZone == null)
				continue;

			GlStateManager.disableDepth();
			GlStateManager.enableDepth();

			Vec3 pos = player().getPositionVector();
			if (bot.isVecInsideOrTouching(pos) || bot.isVecNearby(pos, nearbyRadius.get()))
				drawTooltip();
		}
	}

	void drawTooltip() {
		String keys;
		if (LABY_4.isActive()) {
			keys = Key.concat(keybind.get().stream().map(Key::get).collect(Collectors.toSet()));
		} else {
			keys = KeySettingImpl.formatKeys(keybind.get());
		}
		BossStatus.bossName = "Botshop-Gui verfügbar! [" + keys + "]";
		BossStatus.statusBarTime = 1;
		BossStatus.healthScale = 0f;
	}

	public void onKeyPress(boolean b) {
		if (!b) return;
		for (BABBot bot : renderedBots.values()) {
			if (bot.botZone == null)
				continue;

			Vec3 pos = player().getPositionVector();
			if (!bot.isVecInsideOrTouching(pos) && !bot.isVecNearby(pos, nearbyRadius.get()))
				continue;

			bot.sync().whenComplete((dataPresent, ex) -> {
				if (ex != null || !Boolean.TRUE.equals(dataPresent))
					return;
				mc().addScheduledTask(() -> {
					if (world() == null || player() == null)
						return;
					boolean readOnly = !bot.isVecInsideOrTouching(player().getPositionVector());
					mc().displayGuiScreen(new BotshopGUI(bot, readOnly));
				});
			});
		}
	}
}