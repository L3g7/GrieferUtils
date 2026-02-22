package dev.l3g7.griefer_utils.features.widgets.griefer_pass;

import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.api.event_bus.Priority;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.MessageEvent;

@Singleton
public class GrieferPass {

	private static AbstractQuest quest;

	@EventListener(priority = Priority.HIGHEST)
	private static void onMessageSend(MessageEvent.MessageSendEvent event) {
		if (!event.message.toLowerCase().startsWith("/gu:gp "))
			return;

		// Temp stuff to develop the quests before the gui stuff :D

		String cmd = event.message.substring("/gu:gp ".length());
		String lowerCmd = cmd.toLowerCase();
		if (lowerCmd.contains("init")) {
			if (quest != null)
				EventRegisterer.unregister(quest);
			quest = Quests.parseQuest(cmd.substring("init ".length()));
			LabyBridge.display(quest == null ? "?" : quest.format());
		} else if (lowerCmd.contains("add")) {
			quest.increaseAmount(Integer.parseInt(lowerCmd.substring("set ".length())));
		} else if (lowerCmd.contains("dump")) {
			LabyBridge.display(quest.format());
		} else {
			LabyBridge.display("what");
		}

		event.cancel();
	}

}
