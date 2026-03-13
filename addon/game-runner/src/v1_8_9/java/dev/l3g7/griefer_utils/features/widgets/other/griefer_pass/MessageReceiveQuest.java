package dev.l3g7.griefer_utils.features.widgets.other.griefer_pass;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketSendEvent;
import dev.l3g7.griefer_utils.core.misc.NameCache;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.util.IChatComponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.JOB_SELL_PATTERN;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.ORB_SELL_PATTERN;

abstract class MessageReceiveQuest extends AbstractQuest {

	private static String lastMessageSent = "";

	@EventListener
	private static void onMessageSent(PacketSendEvent<C01PacketChatMessage> event) {
		lastMessageSent = event.packet.getMessage().trim().toLowerCase();
	}

	@EventListener
	private void onMessageReceive(MessageReceiveEvent event) {
		if (event.type != 2)
			increaseAmount(processMessage(event.message));
	}

	protected abstract int processMessage(IChatComponent message);

	static class PHQuest extends MessageReceiveQuest {
		@Override
		protected int processMessage(IChatComponent message) {
			return (lastMessageSent.equals("/p h") || lastMessageSent.startsWith("/p h "))
				&& message.getFormattedText().startsWith("§r§8[§r§6GrieferGames§r§8] §r§aDu wurdest zum Grundstück teleportiert.") ? 1 : 0;
		}

	}

	static class OrbQuest extends MessageReceiveQuest {
		@Override
		protected int processMessage(IChatComponent message) {
			Matcher matcher = ORB_SELL_PATTERN.matcher(message.getUnformattedText());
			if (!matcher.matches())
				return 0;

			double orbsGained = Double.parseDouble(matcher.group("orbs").replace(".", "").replace(',', '.'));
			return (int) orbsGained;
		}
	}

	static class AdventurerQuest extends MessageReceiveQuest {
		private static final Pattern COMPLETE_PATTERN = Pattern.compile("§r§8\\[§r§6Adventure§r§8] §r§aDu hast die §r§6(?:Tägliche|Wöchentliche|Monatliche) Aufgabe§r§a erfolgreich abgeschlossen\\. Du erhältst §r§2\\d+§r§a Adventure-Coin\\(s\\)!§r");

		@Override
		protected int processMessage(IChatComponent message) {
			return COMPLETE_PATTERN.matcher(message.getFormattedText()).matches() ? 1 : 0;
		}
	}

	static class PayQuest extends MessageReceiveQuest {

		@Override
		protected int processMessage(IChatComponent message) {
			if (!lastMessageSent.startsWith("/pay") || lastMessageSent.contains("*"))
				return 0;

			Matcher matcher = Constants.PAYMENT_SEND_PATTERN.matcher(message.getFormattedText());
			if (!matcher.matches())
				return 0;

			double moneyPayed = Double.parseDouble(matcher.group("amount").replace(",", ""));
			return (int) moneyPayed;
		}
	}

	static class WriteMessageQuest extends MessageReceiveQuest {
		@Override
		protected int processMessage(IChatComponent message) {
			Matcher matcher = Constants.GLOBAL_RECEIVE_PATTERN.matcher(message.getFormattedText());
			if (!matcher.matches())
				matcher = Constants.PLOTCHAT_RECEIVE_PATTERN.matcher(message.getFormattedText());
			if (!matcher.matches())
				return 0;

			String name = NameCache.ensureRealName(matcher.group("name").replaceAll("§.", ""));
			return MinecraftUtil.name().equals(name) ? 1 : 0;
		}
	}

	static class PlotReceiveQuest extends MessageReceiveQuest {
		private static final Pattern SETOWNER_PATTERN = Pattern.compile("§r§8\\[§r§6GrieferGames§r§8]§r §r§aDu hast ein Grundstück von §r§2[^§]+§r§a erhalten\\. §r§7\\(§r§e/p tp -?\\d+;-?\\d+§r§7\\)§r");

		@Override
		protected int processMessage(IChatComponent message) {
			return message.getFormattedText().equals("§r§8[§r§6GrieferGames§r§8] §r§aDas Grundstück wurde erfolgreich in Besitz genommen.§r")
				|| SETOWNER_PATTERN.matcher(message.getFormattedText()).matches() ? 1 : 0;
		}
	}

	static class DisguiseQuest extends MessageReceiveQuest {
		@Override
		protected int processMessage(IChatComponent message) {
			if (!lastMessageSent.startsWith("/d ") && !lastMessageSent.startsWith("/disguise "))
				return 0;

			String text = message.getUnformattedText();
			return (text.startsWith("Du bist nun als ") && text.endsWith("verkleidet."))
				|| text.equals("Ein anderes Plugin verbietet es dir, diese Aktion auszuführen.") ? 1 : 0;
		}
	}

	static class BidQuest extends MessageReceiveQuest {
		@Override
		protected int processMessage(IChatComponent message) {
			return message.getFormattedText().equals("§r§8[§r§6GrieferGames§r§8] §r§aDein Gebot wurde erfolgreich abgegeben.§r") ? 1 : 0;
		}
	}

	static class JobCreateQuest extends MessageReceiveQuest {
		@Override
		protected int processMessage(IChatComponent message) {
			return message.getFormattedText().equals("§r§8[§r§6GrieferGames§r§8] §r§aDein Auftrag wurde erfolgreich angelegt.§r") ? 1 : 0;
		}
	}

	static class JobFulfillQuest extends MessageReceiveQuest {
		@Override
		protected int processMessage(IChatComponent message) {
			Matcher matcher = JOB_SELL_PATTERN.matcher(message.getFormattedText());
			if (!matcher.matches())
				return 0;

			return Integer.parseInt(matcher.group("count"));
		}
	}

	static class JoinMagicForestQuest extends MessageReceiveQuest {
		@Override
		protected int processMessage(IChatComponent message) {
			String text = message.getUnformattedText();
			if (text.equals("[GrieferGames] " + MinecraftUtil.name() + " hat es in den Zauberwald geschafft!"))
				return 1;

			return text.equals("[GrieferGames] Serverwechsel auf zauberwald wurde gestartet..") && lastMessageSent.equals("/zauberwald accept") ? 1 : 0;
		}
	}

	static class BreakIntoJailQuest extends MessageReceiveQuest {
		@Override
		protected int processMessage(IChatComponent message) {
			return message.getUnformattedText().matches("\\[GrieferGames] Du wurdest wegen Beihilfe zum Ausbruch zum Abbau von \\d+ Obsidianblöcken eingesperrt\\.") ? 1 : 0;
		}
	}

	static class ConnectFourQuest extends MessageReceiveQuest {
		@Override
		protected int processMessage(IChatComponent message) {
			return message.getFormattedText().matches("§r§8\\[§r§64-Gewinnt§r§8]§r §r§aDu hast das Spiel gegen §r§2[^ ]+ §r§agewonnen!§r") ? 1 : 0;
		}
	}

	static class BuyLotteryQuest extends MessageReceiveQuest {
		@Override
		protected int processMessage(IChatComponent message) {
			return message.getUnformattedText().equals("[GrieferGames] Du hast erfolgreich ein Los erworben.") ? 1 : 0;
		}
	}

	static class MergeQuest extends MessageReceiveQuest {
		@Override
		protected int processMessage(IChatComponent message) {
			return message.getUnformattedText().equals("[GrieferGames] Die Grundstücke wurden zusammengeführt.") ? 1 : 0;
		}
	}

	static class SetPlotFlagsQuest extends MessageReceiveQuest {
		@Override
		protected int processMessage(IChatComponent message) {
			return message.getUnformattedText().equals("[GrieferGames] Die Flag wurde erfolgreich hinzugefügt") ? 1 : 0;
		}
	}

}
