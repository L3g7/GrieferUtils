package dev.l3g7.griefer_utils.core.bridges.laby3;

import com.mojang.authlib.GameProfile;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.core.bridges.laby3.settings.HeaderSettingImpl;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.labymod.laby3.bridges.Laby3MinecraftBridge;
import dev.l3g7.griefer_utils.labymod.laby3.events.LabyModAddonsGuiOpenEvent;
import dev.l3g7.griefer_utils.labymod.laby3.settings.Icon;
import net.labymod.ingamechat.tabs.GuiChatNameHistory;
import net.labymod.main.LabyMod;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.utils.texture.DynamicModTexture;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Bridge
@Singleton
@ExclusiveTo(LABY_3)
public class Laby3MinecraftBridgeImpl implements Laby3MinecraftBridge { // TODO remove

	@Override
	public DynamicModTexture createDynamicTexture(String path, String url) {
		return new DynamicModTexture(new ResourceLocation(path), url);
	}

	@Override
	public void displayAchievement(String title, String message) {
		LabyMod.getInstance().getGuiCustomAchievement().displayAchievement("griefer_utils_icon", title, message);
	}

	@EventListener
	private static void onGuiOpen(GuiOpenEvent<LabyModAddonsGui> event) {
		new LabyModAddonsGuiOpenEvent().fire();
	}

	@Override
	public HeaderSetting createDropDownPadding() {
		return new HeaderSettingImpl() {
			public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
				entryHeight(5);
			}
		};
	}

	@Override
	public Pair<String, String> getCachedTexture(UUID uuid) {
		ResourceLocation resourceSkin = LabyMod.getInstance().getDrawUtils().getPlayerSkinTextureCache().getSkinTexture(new GameProfile(uuid, ""));
		if (resourceSkin == null)
			return null;

		return new Pair<>(resourceSkin.getResourceDomain(), resourceSkin.getResourcePath());
	}

	@Override
	public void openNameHistory(String name) {
		if (name.startsWith("!")) {
			labyBridge.notify("§eUngültiger Name", "§fVon Bedrock-Spielern kann kein Namensverlauf abgefragt werden.");
			return;
		}

		mc().displayGuiScreen(new GuiChatNameHistory("", name));
	}

}
