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

package dev.l3g7.griefer_utils.v1_8_9.features.world.better_schematica;

import com.github.lunatrius.schematica.api.event.PreSchematicSaveEvent;
import com.github.lunatrius.schematica.client.world.SchematicWorld;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.api.util.LambdaUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3i;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.v1_8_9.features.world.better_schematica.BetterSchematica.isSavePositionEnabled;

@SuppressWarnings("unused")
public class SaveSchematicaPosition {

	private static File loadedFile;
	private static SchematicWorld schematicWorld = null;
	private static Vec3i position = null;

	public static void addGuiButton(List<GuiButton> buttons, GuiScreen gui) {
		if (!isSavePositionEnabled())
			return;

		GuiButton button = new GuiButton(1 << 30, gui.width - 90, gui.height - 225, 80, 20, "Speichern");
		button.enabled = Reflection.get(gui, "schematic") != null;
		buttons.add(button);
	}

	public static void onActionPerformed(GuiButton button, GuiScreen gui) {
		if (!isSavePositionEnabled())
			return;

		List<GuiButton> buttons = Reflection.get(gui, "buttonList");
		SchematicWorld schematic = Reflection.get(gui, "schematic");

		if (schematic != null && button.enabled && button.id == 1 << 30) {
			schematicWorld = schematic;
			boolean success = SchematicFormat.writeToFile(loadedFile, schematic.getSchematic());
			if (success) {
				labyBridge.notify("§aSchematic gespeichert", "§aDie Schematic und deren Position wurde gespeichert.");
			} else {
				labyBridge.notify("§cFehler", "§cMehr Informationen befinden sich im Chat.");
				labyBridge.displayInChat(Constants.ADDON_PREFIX + "Beim Speichern der Schematic ist ein Fehler aufgetreten. Hast du die Datei mit dem Ingame-Befehl runtergeladen? Wenn ja, wtf. Wenn nein, bitte melde dich mit dem neusten Log im GrieferUtils-Discord.");
			}
		}
	}

	public static void onSchematicLoaded(File directory, String filename) {
		loadedFile = new File(directory, filename);
	}

	public static void readFromNBT(NBTTagCompound tag) {
		if (!isSavePositionEnabled())
			return;

		if (!tag.hasKey("ExtendedMetadata")) {
			position = null;
			return;
		}

		NBTTagCompound metadataTag = tag.getCompoundTag("ExtendedMetadata");

		if (!metadataTag.hasKey("griefer_utils_position_data")) {
			position = null;
			return;
		}

		NBTTagCompound posNbt = metadataTag.getCompoundTag("griefer_utils_position_data");
		int x = posNbt.getInteger("x");
		int y = posNbt.getInteger("y");
		int z = posNbt.getInteger("z");
		position = new Vec3i(x, y, z);
	}

	public static void setPositionAfterLoading(SchematicWorld schematicWorld) {
		if (position == null || !isSavePositionEnabled())
			ClientProxy.moveSchematicToPlayer(schematicWorld);
		else
			schematicWorld.position.set(position);
	}

	private static void onPreSchematicSaveEvent(PreSchematicSaveEvent event) {
		if (schematicWorld == null)
			return;

		NBTTagCompound posNbt = new NBTTagCompound();
		posNbt.setInteger("x", schematicWorld.position.field_177962_a);
		posNbt.setInteger("y", schematicWorld.position.field_177960_b);
		posNbt.setInteger("z", schematicWorld.position.field_177961_c);
		event.extendedMetadata.setTag("griefer_utils_position_data", posNbt);
		schematicWorld = null;
	}

	private static void registerOnPreSchematicSaveEvent() throws ReflectiveOperationException {
		PreSchematicSaveEvent event = new PreSchematicSaveEvent(null, null);
		Object listenerList = Reflection.invoke(event, "getListenerList");

		Object eventPriority = Reflection.get(Class.forName("net.minecraftforge.fml.common.eventhandler.EventPriority"), "NORMAL");

		Method onPreSchematicSaveEventMethod = SaveSchematicaPosition.class.getDeclaredMethod("onPreSchematicSaveEvent", PreSchematicSaveEvent.class);
		Class<?> iEventListenerClass = Class.forName("net.minecraftforge.fml.common.eventhandler.IEventListener");
		Object iEventListener = LambdaUtil.createFunctionalInterface(iEventListenerClass, onPreSchematicSaveEventMethod, SaveSchematicaPosition.class);

		Reflection.invoke(listenerList, "register", 0, eventPriority, iEventListener);
	}

	static {
		if (Constants.SCHEMATICA) {
			try {
				registerOnPreSchematicSaveEvent();
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
