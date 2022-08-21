package dev.l3g7.griefer_utils.features.features;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.chat.MessageSendEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.misc.ItemBuilder;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.ingamechat.IngameChatManager;
import net.labymod.ingamechat.renderer.ChatRenderer;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.event.ClickEvent;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

@Singleton
public class AntiCommandChoker extends Feature {

	private static final String COMMAND = "/grieferutils_anti_command_choker ";
	private static final IngameChatManager IGM = IngameChatManager.INSTANCE;

	private final BooleanSetting enabled = new BooleanSetting()
			.name("Anti Command Choker")
			.description("Verhindert das Senden von Nachrichten, die mit \"7\" beginnen.")
			.config("features.anti_command_choker.active")
			.icon(new ItemBuilder(Blocks.barrier).amount(7))
			.defaultValue(true);

	public AntiCommandChoker() {
		super(Category.FEATURE);
	}

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	@EventListener
	public void onMessageSend(MessageSendEvent event) {
		if (!isActive() || !isOnGrieferGames())
			return;

		String msg = event.getMsg();
		if (msg.startsWith(COMMAND)) {

			String message = msg.substring(COMMAND.length());
			int id = Integer.parseInt(message.split(" ")[0]);
			String command = message.substring(Integer.toString(id).length() + 1);

			send(command);

			// Edit the sent message
			IGM.getSentMessages().remove(IGM.getSentMessages().size() - 1);
			IGM.addToSentMessages(command);

			// Remove the message (everywhere, since I don't know in which chatroom it is)
			for (ChatRenderer chatRenderer : IGM.getChatRenderers())
				clear(chatRenderer, id);
			clear(IGM.getMain(), id);
			clear(IGM.getSecond(), id);

			event.setCanceled(true);
			return;
		}

		if (msg.startsWith("7")) {

			String command = msg.substring(1);
			int id = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

			IChatComponent question = new ChatComponentText(Constants.ADDON_PREFIX + String.format("Meintest du /%s? ", msg.split(" ")[0].substring(1)));

			IChatComponent yes = new ChatComponentText("§a[§l\u2714§r§a] ").setChatStyle(new ChatStyle()
					.setChatClickEvent(getClickEvent("/" + command, id)));

			IChatComponent no = new ChatComponentText("§c[\u2716]").setChatStyle(new ChatStyle()
					.setChatClickEvent(getClickEvent("7" + command, id)));

			mc().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(question.appendSibling(yes).appendSibling(no), id);
			event.setCanceled(true);
		}
	}

	private ClickEvent getClickEvent(String command, int id) {
		return new ClickEvent(ClickEvent.Action.RUN_COMMAND, COMMAND + id + " " + command);
	}

	private void clear(ChatRenderer renderer, int id) {
		renderer.getChatLines().removeIf(line -> line.getChatLineId() == id);
		renderer.getBackendComponents().removeIf(line -> line.getChatLineId() == id);
	}

}
