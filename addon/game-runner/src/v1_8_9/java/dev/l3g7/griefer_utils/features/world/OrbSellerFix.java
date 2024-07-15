/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.core.events.WindowClickEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.network.play.server.S0EPacketSpawnObject;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldSettings;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Mouse;

import java.util.*;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static net.minecraft.network.play.server.S38PacketPlayerListItem.Action.ADD_PLAYER;

@Singleton
public class OrbSellerFix extends Feature {

	private final HashMap<String, Integer> cbToId = new HashMap<>();
	private Pair<Integer, Integer> mousePos = null;
	private UUID orbSellerUUID = null;
	private boolean adding = false;

	private final SwitchSetting restoreMousePos = SwitchSetting.create()
		.name("Maus-Position wiederherstellen")
		.description("Behebt, dass die Maus zur Mitte des Fensters bewegt wird, wenn etwas abgegeben wurde.")
		.icon("mouse");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Orb-Händler fixen")
		.description("Behebt, dass der Orb-Händler nicht sichtbar ist, wenn man sich mit einem Home zu ihm teleportiert."
			 + "\n§8(Der Orbhändler auf dem Citybuild muss dafür seit dem letzten Server-Neustart gesehen worden sein.)")
		.icon("orbseller")
		.subSettings(restoreMousePos);

	@Override
	public void init() {
		super.init();
		String key = getCategory().configKey() + ".orb_seller_fix.";
		if (!Config.has(key + "ids"))
			return;

		JsonObject ids = Config.get(key + "ids").getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : ids.entrySet())
			cbToId.put(entry.getKey(), entry.getValue().getAsInt());

		new Timer().schedule(new TimerTask() {
			public void run() {
				TickScheduler.runAfterRenderTicks(() -> {
					cbToId.clear();
					saveIds();
				}, 1);
			}
		}, new Date(Config.get(key + "reset").getAsLong()), 24 * 3600 * 1000);
	}

	@EventListener
	private void onGuiOpen(GuiOpenEvent<GuiChest> event) {
		if (mousePos == null)
			return;

		TickScheduler.runAfterRenderTicks(() -> {
			Mouse.setCursorPosition(mousePos.getLeft(), mousePos.getRight());
			mousePos = null;
		}, 1);
	}

	@EventListener
	private void onWindowClick(WindowClickEvent event) {
		if (!restoreMousePos.get() || !getGuiChestTitle().startsWith("§6Orbs - Verkauf "))
			return;

		if (event.mode > 4)
			return;

		if (!ItemUtil.getLastLore(event.itemStack).endsWith("Orbs zu verkaufen."))
			return;

		mousePos = Pair.of(Mouse.getX(), Mouse.getY());
	}

	@EventListener
	private void onPacket(PacketReceiveEvent<Packet<?>> event) {
		if (adding)
			return;

		// Detect going to the orb seller
		if (event.packet instanceof S0EPacketSpawnObject p) {
			// Check if it's a ArmorStand
			if (p.getType() != 78)
				return;

			// Check position
			if (p.getX() != 5432 || p.getY() != 788 || p.getZ() != -1176)
				return;

			// Check rotation
			if (p.getYaw() != 62 || p.getPitch() != 26)
				return;

			Integer orbSellerId = cbToId.get(getServerFromScoreboard());
			if (orbSellerId != null && world().getEntityByID(orbSellerId) == null)
				mc().addScheduledTask(() -> spawnOrbSeller(orbSellerId));

		}

		// Get the uuid of the original orb seller
		if (event.packet instanceof S38PacketPlayerListItem packet) {
			if (packet.getAction() != ADD_PLAYER)
				return;

			for (S38PacketPlayerListItem.AddPlayerData entry : packet.getEntries()) {
				if ("§6Händler".equals(entry.getProfile().getName())) {
					orbSellerUUID = entry.getProfile().getId();
					if (cbToId.containsKey(getServerFromScoreboard()))
						world().removeEntityFromWorld(cbToId.get(getServerFromScoreboard()));
					return;
				}
			}
		}

		// Get the entity id of the original orb seller
		if (event.packet instanceof S0CPacketSpawnPlayer packet && packet.getPlayer().equals(orbSellerUUID)) {
			orbSellerUUID = null;
			cbToId.put(getServerFromScoreboard(), packet.getEntityID());
			saveIds();
		}
	}

	private void spawnOrbSeller(int id) {
		adding = true;
		GameProfile gp = new GameProfile(UUID.randomUUID(), "§6Händler");
		Property texture = new Property("textures", Base64.getEncoder().encodeToString("{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/ef686fbf016e1458f7f9a9850f38ef7fd6ae1d28bc241ac0c0686f77fdbb4b61\"}}}".getBytes(UTF_8)));
		gp.getProperties().put("textures", texture);

		// Add seller to tab list, required for the S0CPacketSpawnPlayer and the skin
		S38PacketPlayerListItem s38 = new S38PacketPlayerListItem();
		Reflection.set(s38, "action", ADD_PLAYER);
		s38.getEntries().add(s38.new AddPlayerData(gp, 0, WorldSettings.GameType.SURVIVAL, new ChatComponentText("§6Händler")));
		s38.processPacket(mc().getNetHandler());

		// Spawn the seller himself
		EntityOtherPlayerMP player = new EntityOtherPlayerMP(world(), gp);
		player.setEntityId(id);
		float yaw = (float) Math.toDegrees(Math.atan2(player().posZ + 41.5, player().posX - 172.5));
		yaw -= 90;
		player.setPositionAndRotation(172.5, 26, -41.5, yaw, 0);
		new S0CPacketSpawnPlayer(player).processPacket(mc().getNetHandler());
		world().getEntityByID(id).setRotationYawHead(yaw);

		// Remove player from tab list
		Reflection.set(s38, "action", S38PacketPlayerListItem.Action.REMOVE_PLAYER);
		s38.processPacket(mc().getNetHandler());
		adding = false;
	}

	private void saveIds() {
		String key = getCategory().configKey() + ".orb_seller_fix.";
		Config.set(key + "reset", new JsonPrimitive(getNextServerRestart()));

		JsonObject ids = new JsonObject();
		for (Map.Entry<String, Integer> entry : cbToId.entrySet())
			ids.addProperty(entry.getKey(), entry.getValue());

		Config.set(key + "ids", ids);
		Config.save();
	}

}
