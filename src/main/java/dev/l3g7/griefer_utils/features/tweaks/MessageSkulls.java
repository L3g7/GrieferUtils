package dev.l3g7.griefer_utils.features.tweaks;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.chat.MessageModifyEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.misc.NameCache;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.ingamechat.renderer.ChatLine;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.ModColor;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.misc.Constants.MESSAGE_PATTERNS;

@Singleton
public class MessageSkulls extends Feature {

	private static final String SPACE = "§m§e§s§s§a§g§e§s§k§u§l§l§s§r   ";
	private static final ArrayList<Pattern> PATTERNS = new ArrayList<Pattern>(MESSAGE_PATTERNS) {{add(Constants.STATUS_PATTERN);}};

	private final BooleanSetting enabled = new BooleanSetting()
		.name("Kopf vor Nachrichten")
		.description("Zeigt den Kopf des Autors vor Nachrichten im öffentlichen Chat an.")
		.config("tweaks.message_skulls.active")
		.icon("steve")
		.defaultValue(false);

	public MessageSkulls() {
		super(Category.TWEAK);
	}

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	@EventListener
	public void onMsgReceive(MessageModifyEvent event) {
		if (!isActive() || !isOnGrieferGames())
			return;

		for (Pattern pattern : PATTERNS) {
			Matcher matcher = pattern.matcher(event.getMessage().getFormattedText());

			if (matcher.matches()) {

				event.setMessage(new ChatComponentText(SPACE).appendSibling(event.getMessage()));
				return;
			}
		}
	}

	@SuppressWarnings("unused")
	public static void renderSkull(ChatLine line, int y) {

		IChatComponent component = (IChatComponent) line.getComponent();
		String formattedText = component.getFormattedText();
		int spaceIndex = formattedText.indexOf(SPACE);

		if (spaceIndex == -1)
			return;

		String msg = ModColor.removeColor(formattedText.substring(spaceIndex + SPACE.length()));

		int startIndex = msg.indexOf('\u2503') + 2;
		int endIndex;
		int arrowIndex = msg.indexOf('\u00bb');

		if (arrowIndex != -1)
			endIndex = arrowIndex - 1;
		else if (msg.startsWith("[Plot-Chat]"))
			endIndex = msg.indexOf(':') - 1;
		else if (msg.startsWith("[") && msg.contains(" -> mir]"))
			endIndex = msg.indexOf('-') - 1;
		else if (msg.startsWith("[mir -> "))
			endIndex = msg.indexOf(']');
		else
			endIndex = msg.indexOf(' ', startIndex);

		String name = msg.substring(startIndex, endIndex);
		NetworkPlayerInfo playerInfo = mc().getNetHandler().getPlayerInfo(name.contains("~") ? NameCache.getName(name) : name);
		if (playerInfo == null)
			return;


		DrawUtils drawUtils = LabyMod.getInstance().getDrawUtils();
		drawUtils.bindTexture(playerInfo.getLocationSkin());
		int x = drawUtils.getStringWidth(formattedText.substring(0, spaceIndex)) + (formattedText.startsWith("§r" + SPACE) ? 2 : 1);
		drawUtils.drawTexture(x, y - 8, 32, 32, 32, 32, 8, 8); // First layer
		drawUtils.drawTexture(x, y - 8, 160, 32, 32, 32, 8, 8); // Second layer
	}

}
