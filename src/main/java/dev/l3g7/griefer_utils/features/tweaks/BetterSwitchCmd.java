package dev.l3g7.griefer_utils.features.tweaks;

import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.chat.MessageSendEvent;
import dev.l3g7.griefer_utils.event.events.server.CityBuildJoinEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class BetterSwitchCmd extends Feature {

	private static final String ALL = getRegex("\\S+");
	private static final Map<String, String> SPECIAL_SERVERS = new HashMap<String, String>(){{
			put("n|nature", "nature");
			put("x|extreme", "extreme");
			put("e|evil", "cbevil");
			put("w|wasser", "farm1");
			put("l|lava", "nether1");
			put("v|event", "eventserver");
	}};

	private static final String NUMBER = getRegex("\\d+");

	private static String getRegex(String cityBuild) {
		return "(?i)^/(?:cb ?|switch )(" + cityBuild + ")(?: (.*))?$";
	}

	private String command = "";
	private boolean awaitingSendCommand = false;

	private final BooleanSetting enabled = new BooleanSetting()
			.name("BetterSwitchCmd")
			.config("tweaks.better_switch_cmd.active")
			.description("Fügt Aliasse für /switch <cb> hinzu (siehe '/cb').\n" +
					"Der Text nach dem Citybuild wird beim Beitritt in den Chat geschrieben.")
			.icon(Material.COMPASS)
			.defaultValue(true);

	public BetterSwitchCmd() {
		super(Category.TWEAK);
	}

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	@EventListener
	public void onMessageSend(MessageSendEvent event) {
		if (!isActive() || !isOnGrieferGames())
			return;

		if (awaitingSendCommand) {
			awaitingSendCommand = false;
			return;
		}

		String msg = event.getMsg();

		if (msg.equals("/cb")) {
			display(Constants.ADDON_PREFIX + "Syntax: '/switch <CB> [command]', '/cb <CB> [command]' oder '/cb<CB> [command]'.");
			display(Constants.ADDON_PREFIX + "§7§nShortcuts:");
			display(Constants.ADDON_PREFIX + "§7Nature: 'n'");
			display(Constants.ADDON_PREFIX + "§7Extreme: 'x'");
			display(Constants.ADDON_PREFIX + "§7Evil: 'e'");
			display(Constants.ADDON_PREFIX + "§7Wasser: 'w'");
			display(Constants.ADDON_PREFIX + "§7Lava: 'l'");
			display(Constants.ADDON_PREFIX + "§7Event: 'v'");
			event.setCanceled(true);
			return;
		}


		for (String s : SPECIAL_SERVERS.keySet())
			if (msg.matches(getRegex(s)))
				join(SPECIAL_SERVERS.get(s), event);

		if (msg.matches(NUMBER))
			join("cb" + msg.replaceAll(NUMBER, "$1"), event);
	}

	public void join(String cb, MessageSendEvent event) {
		awaitingSendCommand = true;
		send("/switch " + cb);
		command = event.getMsg().replaceAll(ALL, "$2");
		event.setCanceled(true);
	}

	@EventListener
	public void onCityBuild(CityBuildJoinEvent event) {

		if (command.isEmpty())
			return;

		send(command);
		command = "";
	}

}
