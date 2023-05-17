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

package dev.l3g7.griefer_utils.misc;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * A helper class since the citybuilds have multiple names
 */
public class Citybuild {

	private static final List<Citybuild> CITYBUILDS = ImmutableList.of(
		new Citybuild("nature", "Nature", "n"),
		new Citybuild("extreme", "Extreme", "x"),
		new Citybuild("cbevil", "Evil", "e", "cbe", "CB Evil"),
		new Citybuild("farm1", "Wasser", "w"),
		new Citybuild("nether1", "Lava", "l"),
		new Citybuild("eventserver", "Event", "v")
	);

	public static Citybuild getCitybuild(String cb) {
		cb = cb.toLowerCase();
		if (cb.startsWith("cb"))
			cb = cb.substring(2).trim();

		if (StringUtils.isNumeric(cb))
			return new Citybuild("cb" + cb, "CB" + cb);

		for (Citybuild citybuild : CITYBUILDS)
			if (citybuild.matches(cb))
				return citybuild;

		return new Citybuild(null, null) {
			public boolean exists() {
				return false;
			}
		};
	}

	private final String switchTarget;
	private final String displayName;
	private final String[] aliases;

	private Citybuild(String switchTarget, String displayName, String... aliases) {
		this.switchTarget = switchTarget;
		this.displayName = displayName;
		this.aliases = aliases;
	}

	public boolean exists() {
		return true;
	}

	public boolean isOnCb() {
		if (!exists())
			throw new IllegalStateException("This citybuild does not exist");

		return matches(MinecraftUtil.getServerFromScoreboard());
	}

	public void join() {
		if (!exists())
			throw new IllegalStateException("This citybuild does not exist");

		MinecraftUtil.send("/switch " + switchTarget);
	}

	public String getSwitchTarget() {
		return switchTarget;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean matches(String  cb) {
		if (cb == null)
			return false;

		for (String alias : aliases)
			if (alias.equalsIgnoreCase(cb))
				return true;

		return displayName.equalsIgnoreCase(cb) || switchTarget.equalsIgnoreCase(cb);
	}

}
