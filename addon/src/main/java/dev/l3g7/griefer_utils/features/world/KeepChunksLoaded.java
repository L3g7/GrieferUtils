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

package dev.l3g7.griefer_utils.features.world;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.core.util.Util;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import io.netty.buffer.Unpooled;
import net.labymod.core.LabyModCore;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.utils.ModColor;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S26PacketMapChunkBulk;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

@Singleton
public class KeepChunksLoaded extends Feature {

	private static final Set<ChunkCoordIntPair> forceLoadedChunks = new HashSet<>();
	private static boolean showingWarning = false;

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Chunks geladen lassen")
		.icon("chunk")
		.description("Lässt Chunks nicht entladen.");

	@Override
	public void init() {
		super.init();
		enabled.callback(b -> {
			if (showingWarning) {
				if (mc().currentScreen instanceof GuiKCLWarning)
					showingWarning = false;
				return;
			}

			if (b) {
				if (Constants.OPTIFINE) {
					showingWarning = true;
					enabled.set(false);
					mc().displayGuiScreen(new GuiKCLWarning((LabyModAddonsGui) mc().currentScreen));
				}
				return;
			}

			if (world() == null)
				return;

			// Unload force loaded chunks
			for (ChunkCoordIntPair chunkCoords : forceLoadedChunks) {
				try {
					PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
					buf.writeInt(chunkCoords.chunkXPos);
					buf.writeInt(chunkCoords.chunkZPos);
					buf.writeBoolean(true);
					buf.writeShort(0);
					buf.writeByteArray(new byte[0]);
					S21PacketChunkData packet = new S21PacketChunkData();
					packet.readPacketData(buf);
					packet.processPacket(mc().getNetHandler());
				} catch (IOException e) {
					throw Util.elevate(e);
				}
			}
		});
	}

	@EventListener
	public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
		if (event.packet instanceof S21PacketChunkData) {
			S21PacketChunkData packet = (S21PacketChunkData) event.packet;
			if (!packet.func_149274_i())
				return;

			ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(packet.getChunkX(), packet.getChunkZ());
			if (packet.getExtractedSize() == 0) {
				forceLoadedChunks.add(chunkCoords);
				event.setCanceled(true);
			} else {
				forceLoadedChunks.remove(chunkCoords);
			}
		}

		if (event.packet instanceof S26PacketMapChunkBulk) {
			S26PacketMapChunkBulk packet = (S26PacketMapChunkBulk) event.packet;
			for (int i = 0; i < packet.getChunkCount(); i++)
				forceLoadedChunks.remove(new ChunkCoordIntPair(packet.getChunkX(i), packet.getChunkZ(i)));
		}
	}

	public static ClassInheritanceMultiMap<Entity>[] getFilledEntityLists(Chunk chunk) {
		ClassInheritanceMultiMap<Entity>[] entityLists = chunk.getEntityLists();

		if (!FileProvider.getSingleton(KeepChunksLoaded.class).isEnabled())
			return entityLists;

		// Remove all entities
		for (ClassInheritanceMultiMap<Entity> map : entityLists) {
			List<Entity> entities = new ArrayList<>(Reflection.get(map, "values"));
			for (Entity entity : entities)
				map.remove(entity);
		}

		// Add them again
		for (Entity entity : world().loadedEntityList) {
			if (entity.chunkCoordX != chunk.xPosition || entity.chunkCoordZ != chunk.zPosition)
				continue;

			if (entity.chunkCoordY < 0 || entity.chunkCoordY > 15)
				continue;

			entityLists[entity.chunkCoordY].add(entity);
		}

		return entityLists;
	}

	private class GuiKCLWarning extends GuiScreen {

		private final LabyModAddonsGui labyModAddonsGui;
		private GuiButton buttonCancel;
		private GuiButton buttonActivate;

		private GuiKCLWarning(LabyModAddonsGui labyModAddonsGui) {
			this.labyModAddonsGui = labyModAddonsGui;
		}

		@Override
		public void initGui() {
			super.initGui();
			buttonList.add(buttonActivate = new GuiButton(2, width / 2 - 95, height / 2 + 20, 90, 20, "Aktivieren"));
			buttonList.add(buttonCancel = new GuiButton(3, width / 2 + 5, height / 2 + 20, 90, 20, "Abbrechen"));
		}

		@Override
		public void drawScreen(int mouseX, int mouseY, float partialTicks) {
			super.drawScreen(mouseX, mouseY, partialTicks);
			labyModAddonsGui.drawScreen(mouseX, mouseY, partialTicks);

			drawUtils().drawIngameBackground();

			GuiScreen gui = mc().currentScreen;
			drawUtils().drawRectangle(gui.width / 2 - 100 - 2, gui.height / 2 - 50 - 2, gui.width / 2 + 100 + 2, gui.height / 2 + 50 + 2, Integer.MIN_VALUE);
			drawUtils().drawRectangle(gui.width / 2 - 100, gui.height / 2 - 50, gui.width / 2 + 100, gui.height / 2 + 50, ModColor.toRGB(20, 20, 20, 144));
			LabyModCore.getMinecraft().drawButton(buttonActivate, mouseX, mouseY);
			LabyModCore.getMinecraft().drawButton(buttonCancel, mouseX, mouseY);
			drawUtils().drawCenteredString("§cWarnung", gui.width / 2d, gui.height / 2d - 44);
			List<String> list = drawUtils().listFormattedStringToWidth("§4Chunks geladen kann zusammen mit OptiFine zu unsichtbaren Entities führen.", 190);
			int lineY = 0;

			for (String s : list) {
				drawUtils().drawCenteredString("§4" + s, gui.width / 2d, gui.height / 2d - 30 + lineY);
				lineY += 10;
			}
		}

		protected void actionPerformed(GuiButton button) throws IOException {
			super.actionPerformed(button);
			if (button == buttonCancel) {
				showingWarning = false;
				mc().displayGuiScreen(labyModAddonsGui);
			}

			if (button == buttonActivate) {
				enabled.set(true);
				mc().displayGuiScreen(labyModAddonsGui);
			}

		}

	}

}
