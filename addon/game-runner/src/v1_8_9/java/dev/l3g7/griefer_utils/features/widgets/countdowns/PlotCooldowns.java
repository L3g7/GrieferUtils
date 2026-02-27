package dev.l3g7.griefer_utils.features.widgets.countdowns;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Citybuild;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageSendEvent;
import dev.l3g7.griefer_utils.core.misc.Countdown;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.widgets.Widget.ComplexWidget;
import net.minecraft.util.ChatComponentText;

import java.util.*;

@Singleton
public class PlotCooldowns extends ComplexWidget {

	private final Map<String, Countdown> countdowns = new HashMap<>();
	private static final List<String> COMMANDS = Arrays.asList("rand", "wand", "boden");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Plot-Cooldowns")
		.description("Zeigt dir die Cooldowns für /rand, /wand und /boden an.")
		.icon("hourglass");

	@Override
	public boolean isVisibleInGame() {
		return !countdowns.isEmpty() && !countdowns.values().stream().allMatch(Countdown::isExpired);
	}

	@Override
	public KVPair[] getLines() {
		List<Pair<String, String>> lines = new ArrayList<>();
		lines.add(new Pair<>("Plot-Cooldowns", ""));
		countdowns.entrySet().stream()
			.filter(e -> !e.getValue().isExpired())
			.sorted(Comparator.comparingInt(e -> e.getValue().secondsRemaining()))
			.forEachOrdered(e -> lines.add(new Pair<>("/" + e.getKey(), e.getValue().secondsRemaining() + "s")));

		return lines.stream()
			.map(e -> new KVPair( new ChatComponentText(e.a), new ChatComponentText(e.b) ))
			.toArray(KVPair[]::new);
	}

	@EventListener
	private void onMessageSend(MessageSendEvent event) {
		if (!event.message.startsWith("/"))
			return;

		Citybuild cb = MinecraftUtil.getCurrentCitybuild();
		if (cb == Citybuild.ANY || cb == Citybuild.MAGIC_FOREST || cb == Citybuild.LAVA || cb == Citybuild.WATER)
			return;

		String command = event.message.substring(1).split(" ")[0];
		if (COMMANDS.contains(command.toLowerCase())) {
			Countdown countdown = countdowns.computeIfAbsent(command, c -> Countdown.realtime());
			if (countdown.isExpired())
				countdown.set(30);
		}
	}
}
