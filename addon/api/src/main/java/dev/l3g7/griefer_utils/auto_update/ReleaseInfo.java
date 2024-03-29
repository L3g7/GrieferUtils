/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.auto_update;

import dev.l3g7.griefer_utils.api.misc.Named;

public class ReleaseInfo {

	String version;
	String hash; // SHA-256

	public enum ReleaseChannel implements Named {

		STABLE("Stabil", "https://github.com/L3g7/GrieferUtils/releases/download/v{version}/griefer-utils-v{version}.jar"),
		BETA("Beta", "https://grieferutils.l3g7.dev/v4/nightly_builds/griefer-utils-v{version}.jar");

		final String name;
		final String downloadURL;

		ReleaseChannel(String name, String downloadURL) {
			this.name = name;
			this.downloadURL = downloadURL;
		}

		@Override
		public String getName() {
			return name;
		}

	}

}
