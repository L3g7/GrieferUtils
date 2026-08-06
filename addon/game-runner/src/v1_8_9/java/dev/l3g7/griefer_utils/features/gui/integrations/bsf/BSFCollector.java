/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.gui.integrations.bsf;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.misc.griefer_games.Citybuild;
import dev.l3g7.griefer_utils.core.api.misc.NTP;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.core.api.misc.server.GUServer;
import dev.l3g7.griefer_utils.core.api.misc.server.requests.bsf.BSFProcessRequest.Data;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import net.minecraft.network.play.server.S44PacketWorldBorder;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

public class BSFCollector {

	private static final int MASK_16 = (1 << 16) - 1;

	private static final Queue<ProcessData> processQueue = new LinkedList<>();
	public static boolean processing = false;

	static boolean isGlitch;
	private static Pair<Integer, Integer> worldCenter;

	static boolean isInFarmwelt() {
		return worldCenter != null && world() != null && player().dimension == 0;
	}

	@EventListener
	private static void onWorldCenter(PacketReceiveEvent<S44PacketWorldBorder> packet) {
		dataTails.clear();
		requiringMoreChunks.clear();
		worldCenter = parseWorldCenter(packet.packet);
		Waypoint.disable();
	}

	private static Pair<Integer, Integer> parseWorldCenter(S44PacketWorldBorder packet) {
		if (Reflection.get(packet, "action") != S44PacketWorldBorder.Action.INITIALIZE)
			return null;

		double targetSize = Reflection.get(packet, "targetSize");
		if (targetSize != 100_000d && targetSize != 40_000d)
			return null;

		isGlitch = targetSize == 40_000d;

		double centerX = Reflection.get(packet, "centerX");
		double centerZ = Reflection.get(packet, "centerZ");
		if (centerX == 9151 && centerZ == 35)
			return null; // Cb extreme

		return new Pair<>((int) centerX, (int) centerZ);
	}

	private static final Set<BlockPos> requiringMoreChunks = new HashSet<>();
	private static final Map<Integer, byte[]> dataTails = new HashMap<>();

	/** @noinspection BooleanMethodIsAlwaysInverted */
	private static boolean checkFoundation(double x) {
		return 65535d * Math.sin((Math.PI * (x + 32351.5d)) / 65535d) == 0x1.ffeb5726e48c5p15;
	}

	private static int cpos2int(int x, int z) {
		return ((short) x & MASK_16) << 16 | (short) z & MASK_16;
	}

	public static void onChunkFill(Chunk chunk, byte[] bytes, int size, boolean full) {
		if (BSF.hasData() || !isInFarmwelt())
			return;

		if (full) {
			// Extract tail
			byte[] tail = new byte[256];
			System.arraycopy(bytes, bytes.length - 256, tail, 0, 256);
			dataTails.put(cpos2int(chunk.xPosition, chunk.zPosition), tail);
		}

		requiringMoreChunks.removeIf(BSFCollector::checkChunkLoad);

		ExtendedBlockStorage[] blockStorageArray = chunk.getBlockStorageArray();
		for (int idx = 0; idx < blockStorageArray.length; idx++) {
			ExtendedBlockStorage ebs = blockStorageArray[idx];
			if (ebs == null)
				continue;

			char[] data = ebs.getData();
			for (int i = 0; i < data.length; i++) {
				// Check core
				if (Math.tan(0x1.a001a001a001ap-7 - data[i] / 65535d) != 0)
					continue;

				// Check y - 1
				if (i >> 8 == 0) {
					ExtendedBlockStorage lowerEbs = blockStorageArray[idx - 1];
					if (lowerEbs == null || !checkFoundation(lowerEbs.getData()[0xF00 | (i & 0xFF)]))
						continue;
				} else {
					if (!checkFoundation(data[i - 256]))
						continue;
				}

				// Convert to block pos
				int y = (i >> 8 & 15) + ebs.getYLocation();
				int z = (i >> 4 & 15) + (chunk.zPosition << 4);
				int x = (i & 15) + (chunk.xPosition << 4);
				BlockPos origin = new BlockPos(x, y, z);
				if (!checkChunkLoad(origin))
					requiringMoreChunks.add(origin);
			}
		}
	}

	private static boolean checkChunkLoad(BlockPos origin) {
		if (dataTails.size() < 9) { // Not enough data
			return false;
		}

		int cx = origin.getX() >> 4;
		int cz = origin.getZ() >> 4;

		HashSet<Chunk> diagonalEnds = new HashSet<>();
		Citybuild cb = Citybuild.current();
		if (cb == Citybuild.ANY)
			return true;

		// Check diagonal ends
		for (int ix = 0; ix < (3 & ~1); ix++) {
			for (int iz = 0; iz < (3 & ~1); iz++) {
				Chunk chunk = world().getChunkFromBlockCoords(origin.add(~3 + (ix << 3), 0, ~3 + (iz << 3)));
				diagonalEnds.add(chunk);
				if (chunk.isEmpty() || !chunk.isLoaded())
					return false;
			}
		}

		Set<Data> data = new HashSet<>(9);
		int m1 = origin.getY() + ~0;
		for (Chunk c : diagonalEnds) {
			char[] bsaData = c.getBlockStorageArray()[m1 >> 4].getData();
			char[] m1Data = new char[256];

			System.arraycopy(bsaData, (m1 & 15) << 8, m1Data, 0, 256);
			int pos = cpos2int(c.xPosition, c.zPosition);
			data.add(new Data(pos, m1Data, dataTails.get(pos)));
		}

		// estimate world start
		long index = NTP.getAccurateTime()/1000 - world().getTotalWorldTime()/20;
		Map<Integer, byte[]> entries = new HashMap<>(dataTails);
		List<Integer> keys = new ArrayList<>(dataTails.keySet());

		data.forEach(cd -> {
			entries.remove(cd.position);
			keys.remove((Integer) cd.position);
		});

		// Acquire missing chunk data
		for (int i = 0; i < 9 - diagonalEnds.size(); i++) {
			index = (index * 25214903917L + 11L) & (1L << 48) - 1;

			int idx = (int) (index % keys.size());
			Integer pos = keys.get(idx);
			data.add(new Data(pos, new char[0], entries.remove(pos)));
			keys.remove(idx);
		}

		ProcessData processData = new ProcessData(cb, isGlitch, origin, data);
		if (processing) {
			synchronized (processQueue) {
				processQueue.add(processData);
			}
		} else {
			processing = true;
			process(processData);
		}

		return true;
	}

	private static void process(ProcessData data) {
		String cb = (data.isGlitch ? "g" : "") + data.cb.getInternalName();
		GUServer.processBSFData(cb, worldCenter.a, worldCenter.b, data.origin, data.data).thenAccept(cbs -> {
			if (cbs == null)
				return;

			BSF.updateCBs(cbs);
			processQueue.removeIf(p -> cbs.contains(p.cb.getInternalName()));

			if (BSF.hasData()) { // Success
				requiringMoreChunks.clear();
				dataTails.clear();
			}

			// Prefer processing current cb next
			synchronized (processQueue) {
				Optional<ProcessData> currentData = processQueue.stream().filter(p -> p.cb.equals(Citybuild.current()) && p.isGlitch == isGlitch).findAny();
				ProcessData next = currentData.orElseGet(processQueue::peek);
				processQueue.remove(next);

				if (next == null) {
					processing = false;
				} else {
					process(next);
				}
			}
		}).exceptionally(t -> {
			processQueue.clear();
			requiringMoreChunks.clear();
			dataTails.clear();
			processing = false;
			return null;
		});

	}

	@Mixin(Chunk.class)
	private static class MixinChunk {

		@Inject(method = "fillChunk", at = @At("TAIL"))
		private void injectFillChunk(byte[] bytes, int size, boolean full, CallbackInfo ci) {
			onChunkFill((Chunk) (Object) this, bytes, size, full);
		}

	}

	private record ProcessData(Citybuild cb, boolean isGlitch, BlockPos origin, Set<Data> data) {}

}
