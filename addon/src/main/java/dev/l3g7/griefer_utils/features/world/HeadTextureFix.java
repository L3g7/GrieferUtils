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

import com.mojang.authlib.GameProfile;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntitySkull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.labyMod;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.uuid;

@Singleton
public class HeadTextureFix extends Feature {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting() {
		@Override
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
			iconData = new IconData(labyMod().getDynamicTextureManager().getTexture("griefer_utils/heads/" + uuid(), "https://crafatar.com/renders/head/" + uuid() + "?scale=5&overlay"));
			super.draw(x, y, maxX, maxY, mouseX, mouseY);
		}
	}
		.name("Kopf-Texturen fixen")
		.description("Lädt Kopf-Texturen automatisch nach.");

	public static final Set<String> lockedProfiles = Collections.synchronizedSet(new HashSet<>());
	public static final Set<String> processedProfiles = Collections.synchronizedSet(new HashSet<>());

	@Mixin(value = TileEntityItemStackRenderer.class, priority = 1001)
	private static class MixinTileEntityItemStackRenderer {

		@Redirect(method = "renderByItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTUtil;readGameProfileFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)Lcom/mojang/authlib/GameProfile;"))
		private GameProfile redirectReadGameProfile(NBTTagCompound nbtTag) {
			GameProfile gameprofile = NBTUtil.readGameProfileFromNBT(nbtTag);
			if (!FileProvider.getSingleton(HeadTextureFix.class).isEnabled())
				return gameprofile;

			String name = gameprofile.getName();

			if (lockedProfiles.contains(name))
				return gameprofile;

			if (!processedProfiles.contains(name)) {
				GameProfile profile = gameprofile;
				lockedProfiles.add(name);
				new Thread(() -> {
					TileEntitySkull.updateGameprofile(profile);
					processedProfiles.add(name);
					lockedProfiles.remove(name);
				}).start();
			} else
				gameprofile = TileEntitySkull.updateGameprofile(gameprofile);

			nbtTag.removeTag("SkullOwner");
			nbtTag.setTag("SkullOwner", NBTUtil.writeGameProfile(new NBTTagCompound(), gameprofile));
			return gameprofile;
		}

	}

}
