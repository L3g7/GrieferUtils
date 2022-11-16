package dev.l3g7.griefer_utils.settings.elements.playerlist;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.l3g7.griefer_utils.features.features.player_list.PlayerListProvider;
import dev.l3g7.griefer_utils.misc.TickScheduler;
import dev.l3g7.griefer_utils.settings.elements.StringSetting;
import dev.l3g7.griefer_utils.util.IOUtil;
import dev.l3g7.griefer_utils.util.Reflection;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import net.labymod.main.LabyMod;
import net.labymod.main.ModTextures;
import net.labymod.utils.Consumer;
import net.labymod.utils.DrawUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.Map;

public class PlayerSetting extends StringSetting {
	private static final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("GrieferUtils' PlayerListEntry IO", false, Thread.MIN_PRIORITY));
	private static final JsonParser PARSER = new JsonParser();
	private Consumer<PlayerSetting> playerSettingConsumer;
	private String previousName = "";
	private String uuid = null;
	private String url = null;
	private DynamicTexture head = null;
	private int updateCounter = 0;

	public PlayerSetting() {
		set("");
		MinecraftForge.EVENT_BUS.register(this);
		icon(ModTextures.MISC_HEAD_QUESTION);

		callback(name -> {
			String prevName = previousName;
			previousName = name;
			if (prevName.equals(name))
				return;

			if (playerSettingConsumer != null)
				playerSettingConsumer.accept(this);

			description(uuid = null);
			head = null;

			if (!name.matches("\\w{3,16}"))
				return;

			updateCounter = 10;
		});

	}

	private void requestUUID() {
		IOUtil.HttpRequest uuidRequest = IOUtil.request("https://api.mojang.com/users/profiles/minecraft/" + get());
		if (uuidRequest.getResponseCode() != 200) {
			if (uuidRequest.getResponseCode() == 429)
				description("§cDu wurdest geratelimited!");
			return;
		}

		uuidRequest.asJsonObject(uuidResponse -> {
			description("§aUUID: §f" + (uuid = uuidResponse.get("id").getAsString()));
			requestSkinUrl();
		});
	}

	private void requestSkinUrl() {
		IOUtil.HttpRequest skinUrlRequest = IOUtil.request("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
		if (skinUrlRequest.getResponseCode() != 200)
			return;

		skinUrlRequest.asJsonObject(skinUrlResponse -> {
			for (JsonElement jsonElement : skinUrlResponse.getAsJsonArray("properties")) {
				JsonObject property = jsonElement.getAsJsonObject();

				if (!property.get("name").getAsString().equals("textures"))
					continue;

				String b64 = property.get("value").getAsString();
				JsonObject o = PARSER.parse(new String(Base64.getDecoder().decode(b64))).getAsJsonObject();
				url = o.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
				updateIcon();
			}
		});
	}

	private void updateIcon() {
		TickScheduler.runNextRenderTick(() -> {
			try {
				head = new DynamicTexture(ImageIO.read(new URL(url)));
				head.loadTexture(mc.getResourceManager());
			} catch (IOException e) {
				return;
			}

			ResourceLocation location = new ResourceLocation(url);
			mc.getTextureManager().loadTexture(location, head);

			Map<ResourceLocation, ITextureObject> mapTextureObjects = Reflection.get(mc.getTextureManager(), "mapTextureObjects");
			mapTextureObjects.put(location, head);
		});
	}

	public void setPlayerSettingConsumer(Consumer<PlayerSetting> consumer) {
		this.playerSettingConsumer = consumer;
	}

	public PlayerListProvider.PlayerListEntry toPlayerListEntry() {
		if (uuid == null)
			return null;

		String formattedUUID = uuid.replaceFirst("(.{8})(.{4})(.{4})(.{4})(.{12})", "$1-$2-$3-$4-$5");

		return new PlayerListProvider.PlayerListEntry(get(), formattedUUID, PlayerListProvider.Provider.USER);
	}

	public static PlayerSetting fromJson(JsonElement element) {
		PlayerSetting setting = new PlayerSetting();

		// Old config
		if (element.isJsonPrimitive()) {
			setting.set(element.getAsString());
			return setting;
		}

		// New config
		JsonObject o = element.getAsJsonObject();

		if (o.has("uuid")) {
			setting.uuid = o.get("uuid").getAsString();
		}

		if (o.has("skinUrl")) {
			setting.url = o.get("skinUrl").getAsString();
			setting.updateIcon();
		}

		setting.set(o.get("name").getAsString());

		return setting;
	}

	public JsonObject toJson() {
		JsonObject object = new JsonObject();

		object.addProperty("name", get());

		if (uuid != null)
			object.addProperty("uuid", uuid);

		if (url != null)
			object.addProperty("skinUrl", url);

		return object;
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);

		if (head == null)
			return;

		DrawUtils drawUtils = LabyMod.getInstance().getDrawUtils();
		GlStateManager.bindTexture(head.getGlTextureId());
		int yHeight = head.getTextureData().length == 4096 ? 32 : 64; // Old textures are 32x64
		drawUtils.drawTexture(x + 3, y + 3, 32, yHeight, 32, yHeight, 16, 16); // First layer
		drawUtils.drawTexture(x + 3, y + 3, 160, yHeight, 32, yHeight, 16, 16); // Second layer
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (--updateCounter > 0)
			return;

		if (updateCounter == 0)
			eventLoopGroup.submit(this::requestUUID);
	}

}
