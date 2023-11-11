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

package dev.l3g7.griefer_utils.features.item.recraft;

import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.config.Config;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.misc.gui.guis.AddonsGuiWithCustomBackButton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.KeySetting;
import dev.l3g7.griefer_utils.settings.elements.components.EntryAddSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.displayAchievement;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

/**
 * Original version by Pleezon
 */
@Singleton
public class Recraft extends Feature {

	static final List<RecraftRecording> recordings = new ArrayList<>();
	static final RecraftRecording tempRecording = new RecraftRecording();

	private final KeySetting key = new KeySetting()
		.name("Letzten Aufruf wiederholen")
		.description("Wiederholt den letzten \"/rezepte\" Aufruf.")
		.icon(ItemUtil.createItem(Blocks.crafting_table, 0, true))
		.pressCallback(pressed -> {
			if (pressed && ServerCheck.isOnCitybuild() && isEnabled())
				RecraftPlayer.play(tempRecording);
		});

	private final EntryAddSetting entryAddSetting = new EntryAddSetting()
		.name("Aufzeichnung hinzufügen");

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Recraft")
		.description("Wiederholt \"/rezepte\" Aufrufe.")
		.icon(ItemUtil.createItem(Blocks.crafting_table, 0, true))
		.subSettings(key, new HeaderSetting(), entryAddSetting);

	@Override
	public void init() {
		super.init();

		entryAddSetting.callback(() -> {
			if (!ServerCheck.isOnCitybuild()) {
				displayAchievement("§cAufzeichnungen", "§ckönnen nur auf einem Citybuild hinzugefügt werden.");
				return;
			}

			RecraftRecording recording = new RecraftRecording();
			enabled.getSubSettings().add(recording.mainSetting);
			recordings.add(recording);
			recording.setTitle("Aufzeichnung hinzufügen");
			mc().displayGuiScreen(new AddonsGuiWithCustomBackButton(() -> {
				recording.setTitle(recording.mainSetting.getDisplayName());
				save();
			}, recording.mainSetting));
		});

		load();
	}

	private void load() {
		if (!Config.has(getConfigKey() + ".recordings"))
			return;

		String encodedRecordings = Config.get(getConfigKey() + ".recordings").getAsString();
		byte[] array = Base64.getDecoder().decode(encodedRecordings);
		PacketBuffer buffer = new PacketBuffer(Unpooled.wrappedBuffer(array));

		byte recordings = buffer.readByte();
		for (int i = 0; i < recordings; i++) {
			RecraftRecording recording = RecraftRecording.read(buffer);
			Recraft.recordings.add(recording);
			enabled.getSubSettings().add(recording.mainSetting);
		}
	}

	void save() {
		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		buffer.writeByte(recordings.size());
		for (RecraftRecording recording : recordings)
			recording.write(buffer);

		byte[] trimmedArray = new byte[buffer.writerIndex()];
		System.arraycopy(buffer.array(), 0, trimmedArray, 0, trimmedArray.length);
		String encodedRecordings = Base64.getEncoder().encodeToString(trimmedArray);
		Config.set(getConfigKey() + ".recordings", new JsonPrimitive(encodedRecordings));
		Config.save();
	}

}
