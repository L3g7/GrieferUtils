package dev.l3g7.griefer_utils.features.widgets.griefer_pass;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import net.minecraft.util.IChatComponent;

import java.util.regex.Matcher;

abstract class AbstractMessageReceiveQuest extends AbstractQuest {

	protected AbstractMessageReceiveQuest(Matcher matcher, int maxAmount) {
		super(matcher, maxAmount);
	}

	@EventListener
	private void onMessageReceive(MessageReceiveEvent event) {
		if (event.type != 2 && processMessage(event.message))
			increaseAmount();
	}

	protected abstract boolean processMessage(IChatComponent message);

}
