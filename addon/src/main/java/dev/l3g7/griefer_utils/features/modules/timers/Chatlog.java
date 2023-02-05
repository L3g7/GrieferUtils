package dev.l3g7.griefer_utils.features.modules.timers;

import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageSendEvent;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.DropDownSetting;
import dev.l3g7.griefer_utils.util.Util;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.List;

@Singleton
public class Chatlog extends Module {

	private boolean sentCmd = false;

	private final DropDownSetting<TimeFormat> timeFormat = new DropDownSetting<>(TimeFormat.class)
		.name("Zeitformat")
		.icon("hourglass")
		.config("modules.chatlog.time_format")
		.defaultValue(TimeFormat.LONG);

	private final BooleanSetting hide = new BooleanSetting()
		.name("Verstecken, wenn fertig")
		.description("Ob das Modul versteckt werden soll, wenn derzeit kein Cooldown existiert.")
		.icon("blindness")
		.config("moduels.chatlog.hide");

	private long chatlogEnd = -1;

	public Chatlog() {
		super("Chatlog", "Zeigt dir den verbleibenden Cooldown bis zum nächsten /chatlog an.", "chatlog", new ControlElement.IconData(Material.WATCH));
	}

	@Override
	public String[] getValues() {
		return new String[]{Util.formatTime(chatlogEnd, timeFormat.get() == TimeFormat.SHORT)};
	}

	@Override
	public String[] getDefaultValues() {
		return new String[]{timeFormat.get() == TimeFormat.SHORT ? "0s" : "0 Sekunden"};
	}

	@Override
	public boolean isShown() {
		return super.isShown() && (!hide.get() || chatlogEnd >= System.currentTimeMillis());
	}

	@EventListener(triggerWhenDisabled = true)
	public void onMessageSend(MessageSendEvent event) {
		String msg = event.message.toLowerCase();

		sentCmd = msg.startsWith("/chatlog") && !event.message.trim().equals("/chatlog");
	}

	@EventListener(triggerWhenDisabled = true)
	public void onMessageReceive(ClientChatReceivedEvent event) {
		if (!sentCmd)
			return;

		String msg = event.message.getUnformattedText();

		if (msg.startsWith("[Chat-Log] Der Chat-Log wurde erfolgreich gespeichert: ") || msg.equals("------------ Chat-Log-Hilfe ------------")) {
			chatlogEnd = System.currentTimeMillis() + 2 * 60 * 1000;
			sentCmd = false;
		}
	}

	@Override
	public void fillSubSettings(List<SettingsElement> list) {
		super.fillSubSettings(list);
		list.add(timeFormat);
		list.add(hide);
	}

	private enum TimeFormat {
		SHORT("Kurz"),
		LONG("Lang");

		private final String name;
		TimeFormat(String name) {
			this.name = name;
		}
	}
}