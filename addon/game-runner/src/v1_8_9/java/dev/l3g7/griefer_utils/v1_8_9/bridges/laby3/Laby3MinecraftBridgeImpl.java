package dev.l3g7.griefer_utils.v1_8_9.bridges.laby3;

import com.mojang.authlib.GameProfile;
import dev.l3g7.griefer_utils.api.bridges.Bridge;
import dev.l3g7.griefer_utils.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Pair;
import dev.l3g7.griefer_utils.laby3.bridges.Laby3MinecraftBridge;
import dev.l3g7.griefer_utils.laby3.bridges.LabyBridgeImpl;
import dev.l3g7.griefer_utils.laby3.events.LabyModAddonsGuiOpenEvent;
import dev.l3g7.griefer_utils.laby3.settings.Icon;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.v1_8_9.bridges.laby3.settings.HeaderSettingImpl;
import dev.l3g7.griefer_utils.v1_8_9.events.GuiScreenEvent.GuiOpenEvent;
import net.labymod.ingamechat.tabs.GuiChatNameHistory;
import net.labymod.main.LabyMod;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.utils.manager.TagManager;
import net.labymod.utils.texture.DynamicModTexture;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.UUID;

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

@Bridge
@Singleton
@ExclusiveTo(LABY_3)
public class Laby3MinecraftBridgeImpl implements Laby3MinecraftBridge {

	@Override
	public DynamicModTexture createDynamicTexture(String path, String url) {
		return new DynamicModTexture(new ResourceLocation(path), url);
	}

	@Override
	public Icon createIcon(Object icon) {
		return IconImpl.of(icon);
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

	@Mixin(value = TagManager.class, remap = false)
	private static class MixinTagManager {

		private static IChatComponent originalMessage;

		@ModifyVariable(method = "tagComponent", at = @At("HEAD"), ordinal = 0, argsOnly = true)
		private static Object injectTagComponent(Object value) {
			originalMessage = ((IChatComponent) value).createCopy();
			return value;
		}

		@ModifyVariable(method = "tagComponent", at = @At(value = "INVOKE", target = "Lnet/labymod/utils/manager/TagManager;getConfigManager()Lnet/labymod/utils/manager/ConfigManager;", shift = At.Shift.BEFORE, ordinal = 0), ordinal = 0, argsOnly = true)
		private static Object injectTagComponentReturn(Object value) {
			return ((LabyBridgeImpl) labyBridge).temp.apply(originalMessage, value);
		}

	}

}
