/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.api.misc.NTP;
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent;
import net.minecraft.network.play.server.S03PacketTimeUpdate;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public abstract class Countdown {

	protected boolean destroyed = false;

	public static Countdown realtime() {
		return new RealtimeCountdown(0);
	}

	public static Countdown ticking() {
		return new TickCountdown(0);
	}

	public void checkWarning(String title, int warnTime) {
		int remaining = secondsRemaining();
		if (remaining <= 0 || remaining >= warnTime)
			return;

		String time = Util.formatTimeSeconds(remaining, true);
		mc().ingameGUI.displayTitle("§c" + title, null, -1, -1, -1);
		mc().ingameGUI.displayTitle(null, "§c§l" + time, -1, -1, -1);
		mc().ingameGUI.displayTitle(null, null, 0, 2, 3);
	}

	public boolean isExpired() {
		return destroyed || secondsRemaining() <= 0;
	}

	public abstract int secondsRemaining();

	public abstract Countdown set(int seconds);

	public Countdown setEnd(long end) {
		long ms = end - NTP.getAccurateTime();
		return set((int) (ms / 1000));
	}

	public abstract void addMinutes(int minutes);

	public abstract void destroy();

	private static class TickCountdown extends Countdown {

		private int secondsRemaining;

		private TickCountdown(int secondsRemaining) {
			this.secondsRemaining = secondsRemaining;
			EventRegisterer.register(this);
		}

		public int secondsRemaining() {
			return secondsRemaining;
		}

		public Countdown set(int seconds) {
			this.secondsRemaining = seconds;
			if (destroyed)
				EventRegisterer.register(this);

			this.destroyed = false;
			return this;
		}

		public void addMinutes(int minutes) {
			secondsRemaining += 60 * minutes;
		}

		public void destroy() {
			destroyed = true;
			EventRegisterer.unregister(this);
		}

		@EventListener
		private void onTimeUpdate(PacketEvent.PacketReceiveEvent<S03PacketTimeUpdate> event) {
			secondsRemaining--;
		}

	}

	private static class RealtimeCountdown extends Countdown {

		private long endTime;

		private RealtimeCountdown(long seconds) {
			this.endTime = System.currentTimeMillis() + seconds * 1000L;
			EventRegisterer.register(this);
		}

		public int secondsRemaining() {
			return (int) Math.ceil((endTime - System.currentTimeMillis()) / 1000d);
		}

		public Countdown set(int seconds) {
			this.endTime = System.currentTimeMillis() + seconds * 1000L;
			this.destroyed = false;
			return this;
		}

		public void addMinutes(int minutes) {
			endTime += 60000L * minutes;
		}

		public void destroy() {
			destroyed = true;
		}

	}

}
