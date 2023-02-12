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

package dev.l3g7.griefer_utils.features.uncategorized.settings.debug.log.log_entries;

import dev.l3g7.griefer_utils.features.uncategorized.settings.debug.log.LogEntry;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.utils.Material;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static dev.l3g7.griefer_utils.core.util.JsonUtil.jsonObject;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ClientMetadataEntry extends LogEntry {

	@MainElement
	private static final BooleanSetting enabled = new BooleanSetting()
		.name("Client-Metadaten")
		.config("settings.debug.log.client_metadata")
		.defaultValue(true)
		.custom("Ja", "Nein")
		.icon(Material.GRASS);

	@Override
	public void addEntry(ZipOutputStream zip) throws IOException {
		zip.putNextEntry(new ZipEntry("client_metadata.json"));

		Runtime runtime = Runtime.getRuntime();
		Minecraft mc = mc();

		ByteArrayOutputStream stack = new ByteArrayOutputStream();
		try (PrintStream ps = new PrintStream(stack, true, "UTF-8")) {
			new Throwable().printStackTrace(ps);
		}
		zip.write(jsonObject(
			"dataDir", mc.mcDataDir.getCanonicalPath(),
			"launchedVersion", mc.getVersion(),
			"proxy", mc.getProxy().toString(),
			"debug", mc.debug,
			"brand", ClientBrandRetriever.getClientModName(),
			"runTime", System.currentTimeMillis() - mc.getPlayerUsageSnooper().getMinecraftStartTimeMillis(),
			"state", mc.getIntegratedServer() != null ? (mc.getIntegratedServer().getPublic() ? "hosting_lan" : "singleplayer") : (mc.getCurrentServerData() != null ? (mc.getCurrentServerData().isOnLAN() ? "playing_lan" : "multiplayer") : "out_of_game"),
			"profilerSection", mc.mcProfiler.profilingEnabled ? mc.mcProfiler.getNameOfLastSection() : "<disabled>",
			"os", System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version"),
			"cpu", OpenGlHelper.getCpu(),
			"memory", runtime.freeMemory() + " of " + runtime.totalMemory() + ", up to " + runtime.maxMemory(),
			"gl", GL11.glGetString(GL11.GL_VENDOR) + " v" + GL11.glGetString(GL11.GL_VERSION) + ", using " + GL11.glGetString(GL11.GL_RENDERER),
			"java", System.getProperty("java.version") + ", " + System.getProperty("java.vendor"),
			"jvm", System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor"),
			"jvmArgs", String.join(", ", ManagementFactory.getRuntimeMXBean().getInputArguments()),
			"stack", stack.toString("UTF-8")
		).toString().getBytes(UTF_8));
		zip.closeEntry();
	}
}