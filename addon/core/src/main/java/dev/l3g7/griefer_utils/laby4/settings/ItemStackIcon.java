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

package dev.l3g7.griefer_utils.laby4.settings;

import net.labymod.api.Laby;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.render.batch.ResourceRenderContext;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.client.world.item.ItemStack;
import net.labymod.api.util.bounds.Rectangle;

import static dev.l3g7.griefer_utils.api.bridges.MinecraftBridge.minecraftBridge;

public class ItemStackIcon extends Icon {

	private final ItemStack icon;

	protected ItemStackIcon(ItemStack icon) {
		super(null);
		this.icon = icon;
	}

	@Override
	public void render(ResourceRenderContext context, float x, float y, float width, float height, boolean hover, int color) {
		render(null, x, y, width, height, hover, color, null);
	}

	@Override
	public void render(Stack stack, float x, float y, float width, float height, boolean hover, int color, Rectangle stencil) {
		// Fix position for scales < 16
		x += -1.5f * width + 24;
		y += -1.25f * height + 20;

		minecraftBridge.scale(width / 16f, height / 16f, 1);
		Laby.labyAPI().minecraft().itemStackRenderer().renderItemStack(stack, icon, (int) x, (int) y);
		minecraftBridge.scale(16f / width, 16f / height, 1);
	}

}
