package dev.l3g7.griefer_utils.laby3.settings;

import static dev.l3g7.griefer_utils.laby3.bridges.Laby3MinecraftBridge.laby3MinecraftBridge;

public abstract class Icon {

	public static Icon of(Object icon) {
		return laby3MinecraftBridge.createIcon(icon);
	}

	public abstract void draw(int x, int y);

}
