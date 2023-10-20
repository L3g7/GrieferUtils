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

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Vec3d;
import dev.l3g7.griefer_utils.event.events.TileEntityDataSetEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.WorldBlockOverlayRenderer;
import dev.l3g7.griefer_utils.misc.WorldBlockOverlayRenderer.RenderObject;
import dev.l3g7.griefer_utils.misc.WorldBlockOverlayRenderer.RenderObjectGenerator;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkCoordIntPair;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;
import static net.labymod.utils.Material.COMPASS;

@Singleton
public class ShowSpawnerIcons extends Feature implements RenderObjectGenerator {

	private final NumberSetting range = new NumberSetting()
		.name("Radius")
		.description("Der Radius um den Spieler in Chunks, in dem die Icons angezeigt werden."
			+ "\n(-1 ist unendlich)")
		.defaultValue(-1)
		.min(-1)
		.icon(COMPASS);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Spawner-Icons anzeigen")
		.description("Zeigt an Spawnern ein Icon des Mobs an, das gespawnt wird.")
		.icon("griefer_info/outlined_mob_icons/Silverfish")
		.subSettings(range);

	@Override
	public void init() {
		super.init();
		WorldBlockOverlayRenderer.registerRenderObjectGenerator(this);
	}

	@Override
	public int getRange() {
		return range.get();
	}

	@Override
	public RenderObject getRenderObject(IBlockState state, BlockPos pos, WorldClient world) {
		if (state.getBlock() != Blocks.mob_spawner)
			return null;

		TileEntity tileEntity = world.getTileEntity(pos);
		if (!(tileEntity instanceof TileEntityMobSpawner))
			return null;

		return new EntityIconRenderObject(tileEntity);
	}

	@EventListener
	private void onTileEntityDataSet(TileEntityDataSetEvent event) {
		if (!(event.tileEntity instanceof TileEntityMobSpawner))
			return;

		ChunkCoordIntPair pair = new ChunkCoordIntPair(event.tileEntity.getPos().getX() >> 4, event.tileEntity.getPos().getZ() >> 4);
		WorldBlockOverlayRenderer.getRenderObjectsForChunk(pair).put(event.tileEntity.getPos(), new EntityIconRenderObject(event.tileEntity));
	}

	private class EntityIconRenderObject extends RenderObject {

		private final ResourceLocation resourceLocation;

		private EntityIconRenderObject(TileEntity tileEntity) {
			super(ShowSpawnerIcons.this);

			TileEntityMobSpawner spawner = (TileEntityMobSpawner) tileEntity;
			Entity entity = spawner.getSpawnerBaseLogic().func_180612_a(world());
			if (entity == null) {
				resourceLocation = new ResourceLocation("missing_entity_texture_null");
				return;
			}

			resourceLocation = new ResourceLocation("griefer_utils/icons/griefer_info/outlined_mob_icons/" + EntityList.getEntityString(entity) + ".png");
		}

		@Override
		public void render(BlockPos pos, float partialTicks) {
			prepareRender(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), partialTicks);

			GlStateManager.translate(0, 0.75, 0);
			GlStateManager.scale(1, -1, 1);

			GlStateManager.color(1, 1, 1, 1);
			mc().getTextureManager().bindTexture(resourceLocation);

			for (int i = 0; i < 4; i++) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(-0.25f, 0, 0.51f);
				GlStateManager.scale(1/512f, 1/512f, 1/512f);
				drawUtils().drawTexturedModalRect(0, 0, 256, 256);
				GlStateManager.popMatrix();
				GlStateManager.rotate(90, 0, 1, 0);
			}

			GlStateManager.pushMatrix();
			GlStateManager.rotate(90, 1, 0, 0);
			GlStateManager.translate(-0.25f, -0.25f, 0.26f);
			GlStateManager.scale(1/512f, 1/512f, 1/512f);
			drawUtils().drawTexturedModalRect(0, 0, 256, 256);
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
			GlStateManager.rotate(90, -1, 0, 0);
			GlStateManager.translate(-0.25f, -0.25f, 0.76f);
			GlStateManager.scale(1/512f, 1/512f, 1/512f);
			drawUtils().drawTexturedModalRect(0, 0, 256, 256);
			GlStateManager.popMatrix();

			GlStateManager.popMatrix();
		}

	}

}
