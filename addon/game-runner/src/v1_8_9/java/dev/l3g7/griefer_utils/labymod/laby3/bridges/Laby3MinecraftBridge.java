package dev.l3g7.griefer_utils.labymod.laby3.bridges;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.Bridged;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.labymod.laby3.settings.Icon;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import net.labymod.utils.texture.DynamicModTexture;

import java.util.UUID;

@Bridged
public interface Laby3MinecraftBridge {

	Laby3MinecraftBridge laby3MinecraftBridge = FileProvider.getBridge(Laby3MinecraftBridge.class);

	DynamicModTexture createDynamicTexture(String path, String url);

	Icon createIcon(Object icon);

	void displayAchievement(String title, String message);

	HeaderSetting createDropDownPadding();

	Pair<String, String> getCachedTexture(UUID uuid);

	void openNameHistory(String name);

}
