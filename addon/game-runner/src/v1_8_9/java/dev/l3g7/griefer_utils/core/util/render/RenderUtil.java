/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util.render;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

import java.awt.*;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static org.lwjgl.opengl.GL11.glColor4f;

/**
 * A utility class for rendering stuff.
 */
public class RenderUtil {

	public static Vec3 scaleVec3(Vec3 vector, double factor) {
		return new Vec3(vector.xCoord * factor, vector.yCoord * factor, vector.zCoord * factor);
	}

	public static Vec3 camPos() {
		Entity entity = mc().getRenderViewEntity();
		Vec3 prevPos = new Vec3(entity.prevPosX, entity.prevPosY, entity.prevPosZ);
		return prevPos.add(scaleVec3(pos(entity).subtract(prevPos), partialTicks()));
	}

	public static void glColor(Color color) {
		glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
	}

}
