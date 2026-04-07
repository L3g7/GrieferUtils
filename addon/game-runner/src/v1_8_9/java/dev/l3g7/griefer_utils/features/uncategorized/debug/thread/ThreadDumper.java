/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.debug.thread;

import dev.l3g7.griefer_utils.core.api.misc.ThreadFactory;
import dev.l3g7.griefer_utils.core.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.uncategorized.debug.DebugSettings;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.MIN_PRIORITY;

public class ThreadDumper {

	private static boolean running = false;
	private static final List<String> dumps = new ArrayList<>();
	private static final File FILE = new File("GrieferUtils/threaddumps.txt");

	private static final SwitchSetting dumpAll = SwitchSetting.create()
		.name("Nur Client Thread dumpen")
		.description("Ob alle Threads, oder nur der Client / Mainthread gedumpt werden sollen.")
		.defaultValue(true)
		.icon("glass_pane");

	private static final NumberSetting interval = NumberSetting.create()
		.name("Intervall")
		.description("Wie viel Zeit zwischen Dumps vergehen soll (in Millisekunden).")
		.icon("clock")
		.min(1)
		.defaultValue(1000);

	private static final NumberSetting maxDumps = NumberSetting.create()
		.name("Maximale Dumps")
		.description("Wie viel Dumps gespeichert werden sollen.", "Vorherige Dumps werden gelöscht.")
		.icon("hopper")
		.min(1)
		.defaultValue(60)
		.callback(dumps::clear);

	public static final SwitchSetting enabled = SwitchSetting.create()
		.name("Thread-Dumper")
		.description("Dumpt in regelmäßigen Abständen die Stacktraces laufender Threads.", "Die Dumps werden in GrieferUtils/threaddumps.txt geschrieben.")
		.icon("glass_pane")
		.subSettings(interval, maxDumps, dumpAll)
		.callback(enabled -> {
			if (DebugSettings.enabled.get())
				tryStartThread();
		});

	public static void tryStartThread() throws IOException {
		if (enabled.get() && running)
			return;

		FILE.getParentFile().mkdirs();
		FILE.createNewFile();
		ThreadFactory.run("GrieferUtils Thread Dumper", MIN_PRIORITY, () -> {
			while (enabled.get()) {
				dumps.add(ThreadDumpGenerator.generateThreadDumps(dumpAll.get()));
				if (dumps.size() > maxDumps.get())
					dumps.remove(0);

				try {
					try (PrintStream ps = new PrintStream(FILE)) {
						for (String dump : dumps) {
							ps.println(dump);
							ps.println("\n");
						}
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				try {
					Thread.sleep(interval.get());
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			running = false;
		});
	}

}
