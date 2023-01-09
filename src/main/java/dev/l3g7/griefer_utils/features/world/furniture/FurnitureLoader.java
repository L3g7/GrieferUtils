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

package dev.l3g7.griefer_utils.features.world.furniture;

import dev.l3g7.griefer_utils.event.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.features.world.furniture.download.AssetsCreator;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.misc.Constants.FURNITURE_RESOURCE_PACK_DIR;

public class FurnitureLoader {

	public static boolean loaded = false;

	@OnEnable
	public static void onPreInit() {
		if (!FileProvider.getSingleton(Furniture.class).isEnabled() || loaded)
			return;

		if (!AssetsCreator.assetsExist()) {
			AssetsCreator.createAssets();
			return;
		}

		List<IResourcePack> defaultResourcePacks = Reflection.get(mc(), "defaultResourcePacks");
		IResourcePack resourcePack = new FolderResourcePack(FURNITURE_RESOURCE_PACK_DIR);
		defaultResourcePacks.add(resourcePack);

		Registerer.registerBlocks();
		if (FMLCommonHandler.instance().getSide().isClient())
			Registerer.registerRenders();

		loaded = true;
	}

}
