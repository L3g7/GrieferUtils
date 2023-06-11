/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.core.auto_update;

public class ReleaseInfo {

	String version;
	String hash; // SHA-256

	public enum ReleaseChannel {

		STABLE("Stabil", "https://github.com/L3g7/GrieferUtils/releases/download/v{version}/griefer-utils-v{version}.jar"),
		BETA("Beta", "https://grieferutils.l3g7.dev/v2/nightly_builds/griefer-utils-v{version}.jar");

		final String name;
		final String downloadURL;

		ReleaseChannel(String name, String downloadURL) {
			this.name = name;
			this.downloadURL = downloadURL;
		}
	}

}
