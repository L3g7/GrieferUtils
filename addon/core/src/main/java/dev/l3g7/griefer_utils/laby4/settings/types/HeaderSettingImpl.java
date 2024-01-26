/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.laby4.settings.types;

import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import net.labymod.api.client.component.Component;
import net.labymod.api.configuration.settings.type.SettingHeader;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HeaderSettingImpl extends SettingHeader implements HeaderSetting {

	private String name;
	private String description = null;
	private List<Component> rows;
	private boolean center = false;

	public HeaderSettingImpl(String name) {
		super("", false, "", "");
		name(name);
		rows = Collections.singletonList(Component.text(name));
	}

	public HeaderSettingImpl(String... rows) {
		super("", false, "", "");
		name("<multiple rows>");
		this.rows = Arrays.stream(rows).map(Component::text).collect(Collectors.toList());
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public HeaderSetting name(String name) {
		this.name = name.trim();
		this.rows = Collections.singletonList(Component.text(name));
		return this;
	}

	@Override
	public Component displayName() {
		return Component.text(name);
	}

	@Override
	public List<Component> getRows() {
		return rows;
	}


	@Override
	public HeaderSetting description(String... description) {
		this.description = String.join("\n", description).trim();
		return this;
	}

	@Override
	public Component getDescription() {
		return description == null ? null : Component.text(description);
	}


	@Override
	public boolean isCenter() {
		return center;
	}

	@Override
	public HeaderSetting center() {
		this.center = true;
		return this;
	}

}
