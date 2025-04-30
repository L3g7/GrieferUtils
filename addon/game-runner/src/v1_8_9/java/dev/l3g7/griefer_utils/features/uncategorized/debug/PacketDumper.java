/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.debug;

import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.mapping.Mapping;
import dev.l3g7.griefer_utils.core.api.misc.UnsafeJsonSerializer;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.api.util.IOUtil;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.core.settings.types.StringSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.Packet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

class PacketDumper {

	private static final OneSidedPacketDumper incoming = new OneSidedPacketDumper("[INCOMING] ", "Eingehende", "00,03,19,3E");
	private static final OneSidedPacketDumper outgoing = new OneSidedPacketDumper("[OUTGOING] ", "Ausgehende", "00,03,04,05,06");

	public static final SwitchSetting enabled = SwitchSetting.create()
		.name("Packet-Dumper")
		.description("Dumpt ein-/ausgehende Packete.")
		.icon(Items.feather)
		.enabled(LabyBridge.labyBridge.activeMapping() != Mapping.OBFUSCATED)
		.subSettings(incoming.enabled, outgoing.enabled);

	@EventListener
	private static void onPacketReceive(PacketReceiveEvent<?> p) {
		if (DebugSettings.enabled.get() && enabled.get())
			incoming.onPacket(p.packet);
	}

	@EventListener
	private static void onPacketSend(PacketSendEvent<?> p) {
		if (DebugSettings.enabled.get() && enabled.get())
			outgoing.onPacket(p.packet);
	}

	private static class OneSidedPacketDumper {

		private final String prefix;
		private List<String> dumpFieldsList = new ArrayList<>();
		private List<String> blacklistList = new ArrayList<>();

		public final SwitchSetting enabled;

		private OneSidedPacketDumper(String prefix, String settingPrefix, String defaultBlackList) {
			this.prefix = prefix;
			this.enabled = SwitchSetting.create()
				.name(settingPrefix + " Packete dumpen")
				.icon(Items.paper)
				.subSettings(
					StringSetting.create()
						.name("Fields dumpen")
						.description("Die IDs der Packete, deren Fields gedumpt werden sollen, getrennt durch \",\".")
						.icon(Blocks.command_block)
						.maxLength(Integer.MAX_VALUE)
						.callback(s -> dumpFieldsList = Arrays.asList(s.split(","))),

					StringSetting.create()
						.name("Blacklist")
						.description("Die IDs der Packete, die nicht angezeigt werden sollen, getrennt durch \",\".")
						.icon(Blocks.hopper)
						.maxLength(Integer.MAX_VALUE)
						.callback(s -> blacklistList = Arrays.asList(s.split(",")))
						.defaultValue(defaultBlackList)
				);
		}

		private void onPacket(Packet<?> packet) {
			if (!enabled.get())
				return;

			String name = packet.getClass().getSimpleName();
			String id = name.substring(1, 3);
			if (blacklistList.contains(id))
				return;

			System.out.println(prefix + "[" + getLastReadTime() + "] " + name);
			if (!dumpFieldsList.contains(id))
				return;

			try {
				System.out.println(IOUtil.gson.toJson(UnsafeJsonSerializer.toJson(packet)));
			} catch (Throwable t) {
				System.out.println("Packet's fields could not be dumped");
			}
		}

		private long getLastReadTime() {
			if (mc().getNetHandler() == null)
				return 0;

			Channel channel = Reflection.get(mc().getNetHandler().getNetworkManager(), "channel"); // Getter is only available in Forge
			ChannelHandler timeoutHandler = channel.pipeline().get("timeout");
			if (timeoutHandler == null)
				return 0;

			return Reflection.get(timeoutHandler, "lastReadTime");
		}

	}

}
