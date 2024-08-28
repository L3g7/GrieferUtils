package dev.l3g7.griefer_utils.labymod.laby3.bridges;

import com.mojang.authlib.GameProfile;
import dev.l3g7.griefer_utils.core.api.BugReporter;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.mapping.Mapping;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.core.api.misc.functions.Predicate;
import dev.l3g7.griefer_utils.core.api.misc.functions.Runnable;
import dev.l3g7.griefer_utils.core.api.util.IOUtil;
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.core.events.AccountSwitchEvent;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.labymod.laby3.settings.types.HeaderSettingImpl;
import dev.l3g7.griefer_utils.labymod.laby3.util.Laby3Util;
import net.labymod.accountmanager.storage.account.Account;
import net.labymod.api.events.MessageSendEvent;
import net.labymod.core.asm.LabyModCoreMod;
import net.labymod.main.LabyMod;
import net.labymod.utils.JsonParse;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.function.BiFunction;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.mapping.Mapping.*;
import static java.nio.charset.StandardCharsets.UTF_8;

@Bridge
@Singleton
@ExclusiveTo(LABY_3)
public class LabyBridgeImpl implements LabyBridge {

	private String addonVersion = null;

	@Override
	public boolean obfuscated() {
		return LabyModCoreMod.isObfuscated();
	}

	@Override
	public Mapping activeMapping() {
		return obfuscated() ? forge() ? SEARGE : OBFUSCATED : UNOBFUSCATED;
	}

	@Override
	public boolean forge() {
		return LabyModCoreMod.isForge();
	}

	@Override
	public String addonVersion() {
		if (addonVersion != null)
			return addonVersion;

		try {
			String addonJson = new String(IOUtil.toByteArray(FileProvider.getData("addon.json")), UTF_8);
			addonVersion = JsonParse.parse(addonJson).getAsJsonObject().get("addonVersion").getAsString();
			return addonVersion;
		} catch (IOException e) {
			throw Util.elevate(e);
		}
	}

	@Override
	public float partialTicks() {
		return LabyMod.getInstance().getPartialTicks();
	}

	@Override
	public int chatButtonWidth() {
		return 0;
	}

	@Override
	public void notify(String title, String message, int ms) {
		LabyMod.getInstance().getGuiCustomAchievement().displayAchievement("griefer_utils_icon", title, message);
	}

	@Override
	public void notifyError(String message) {
		notify("§c§lFehler ⚠", "§c" + message, 15_000);
	}

	@Override
	public void displayInChat(String message) {
		LabyMod.getInstance().displayMessageInChat(message);
	}

	@Override
	public void openWebsite(String url) {
		try {
			Desktop.getDesktop().browse(new URI(url));
		} catch (IOException | URISyntaxException e) {
			throw Util.elevate(e);
		}
	}

	@Override
	public boolean openFile(File file) {
		try {
			Desktop.getDesktop().open(file);
			return true;
		} catch (IOException e) {
			BugReporter.reportError(e);
			return false;
		}
	}

	@Override
	public void copyText(String text) {
		StringSelection sel = new StringSelection(text);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
	}

	@Override
	public void onJoin(Runnable callback) {
		LabyMod.getInstance().getEventManager().registerOnJoin(v -> callback.run());
	}

	@Override
	public void onQuit(Runnable callback) {
		LabyMod.getInstance().getEventManager().registerOnQuit(v -> callback.run());
	}

	@Override
	public void onMessageSend(Predicate<String> callback) {
		LabyMod.getInstance().getEventManager().register(callback::test);
	}

	public BiFunction<Object, Object, Object> messageModifyConsumer;

	@Override
	public void onMessageModify(BiFunction<Object, Object, Object> callback) {
		messageModifyConsumer = callback; // TODO
	}

	@Override
	public boolean trySendMessage(String message) {
		for (MessageSendEvent lmEvent : LabyMod.getInstance().getEventManager().getMessageSend())
			if (lmEvent.onSend(message))
				return true;

		return false;
	}

	@Override
	public HeaderSetting createLaby3DropDownPadding() {
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
		Laby3Util.openNameHistory(name);
	}

	@ExclusiveTo(LABY_3)
	@Mixin(LabyMod.class)
	private static class MixinLabyMod {

		@Inject(method = "setSession", at = @At("TAIL"), remap = false)
		public void injectSetSession(Account account, CallbackInfo ci) {
			new AccountSwitchEvent().fire();
		}

	}

}
