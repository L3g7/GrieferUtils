package dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.requirements;

import com.google.gson.annotations.SerializedName;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;

/**
 * A requirement for a true boolean value in GrieferUtils' config.
 */
public class GrieferUtilsConfigRequirement extends Requirement {

	private final String key;

	@SerializedName("default")
	private final boolean defaultValue;

	public GrieferUtilsConfigRequirement(String key, boolean defaultValue) {
		this.key = key;
		this.defaultValue = defaultValue;
	}

	@Override
	public boolean test() {
		return Config.has(key) ? Config.get(key).getAsJsonPrimitive().getAsBoolean() : defaultValue;
	}

}
