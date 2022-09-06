package dev.l3g7.griefer_utils.features.features.chat_reactor;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import dev.l3g7.griefer_utils.settings.elements.StringSetting;
import net.labymod.utils.Material;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.features.Feature.display;
import static dev.l3g7.griefer_utils.features.Feature.send;

public class ChatReactorEntry {

	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$(\\d+)");

	private final BooleanSetting parseAsRegex;
	private final StringSetting command;
	private final StringSetting name;
	private StringSetting trigger;
	private Pattern pattern = null;

	public ChatReactorEntry() {
		this(false, "", "", "");
	}

	private ChatReactorEntry(boolean defaultParseAsRegex, String defaultTrigger, String defaultName, String defaultCommand) {
		parseAsRegex = new BooleanSetting()
				.name("Modus")
				.icon("Regex")
				.custom("Regex", "Normal")
				.description("Wie der Trigger interpretiert werden soll.")
				.defaultValue(defaultParseAsRegex)
				.callback(isRegex -> {
					trigger.name(isRegex ? "Ausdruck" : "Text");
					trigger.description(isRegex ? "Nach welchem Ausdruck überprüft werden soll." : "Auf welchen Text geachtet werden soll.");
					trigger.set(trigger.get()); // Trigger callback

					ChatReactor.saveEntries();
					ChatReactor.updateSettings();
				});

		trigger = new StringSetting()
				.name("Text")
				.description("Wenn du das hier siehst ist irgendwas falsch gelaufen :<")
				.defaultValue(defaultTrigger)
				.callback(s -> {
					ChatReactor.saveEntries();
					ChatReactor.updateSettings();

					if (trigger == null)
						return;

					validateTrigger();

					if (!parseAsRegex.get())
						return;

					trigger.name(pattern != null ? "Ausdruck" : "§cAusdruck");
					trigger.description(pattern != null ? null : "Der Ausdrück ist ungültig.");
					ChatReactor.updateSettings();
				})
				.icon(Material.PAPER);

		command = new StringSetting()
				.name("Befehl")
				.description("Welcher Befehl ausgeführt werden soll.")
				.defaultValue(defaultCommand)
				.icon(Material.COMMAND)
				.callback(v -> {
					ChatReactor.saveEntries();
					ChatReactor.updateSettings();
				});

		name = new StringSetting()
				.name("Name")
				.description("Wie diese Reaktion hießen soll.")
				.defaultValue(defaultName)
				.icon(Material.NAME_TAG)
				.callback(v -> {
					ChatReactor.saveEntries();
					ChatReactor.updateSettings();
				});

		trigger.set(defaultTrigger); // Trigger callback
	}

	public CategorySetting getSetting() {
		return new CategorySetting()
				.name((isTriggerValid() ? "" : "§c") + name.get())
				.description(isTriggerValid() ? null : "Der Ausdrück ist ungültig.")
				.icon(parseAsRegex.get() ? "regex" : Material.PAPER)
				.subSettings(name, parseAsRegex, trigger, command);
	}

	public boolean isValid() {
		return !StringUtils.isBlank(command.get())
				&& !StringUtils.isBlank(trigger.get());
	}

	private void validateTrigger() {
		try {
			pattern = Pattern.compile(trigger.get());
		} catch (Exception e) {
			pattern = null;
		}
	}

	private boolean isTriggerValid() {
		return !parseAsRegex.get() || pattern != null;
	}

	public JsonObject toJson() {
		JsonObject object = new JsonObject();

		object.addProperty("is_regex", parseAsRegex.get());
		object.addProperty("name", name.get());
		object.addProperty("trigger", trigger.get());
		object.addProperty("command", command.get());

		return object;
	}

	public static ChatReactorEntry fromJson(JsonObject object) {
		return new ChatReactorEntry(
				object.get("is_regex").getAsBoolean(),
				object.get("trigger").getAsString(),
				object.get("name").getAsString(),
				object.get("command").getAsString()
		);
	}

	public void checkMatch(String text) {
		if (!isTriggerValid())
			return;

		if (!parseAsRegex.get()) {
			if (trigger.get().equals(text))
				send(command.get());
			return;
		}

		Matcher matcher = Pattern.compile(trigger.get()).matcher(text);

		if (!matcher.find())
			return;

		String command = this.command.get();

		Matcher replaceMatcher = PLACEHOLDER_PATTERN.matcher(command);

		try {
			while (replaceMatcher.find())
				command = command.replaceFirst(PLACEHOLDER_PATTERN.pattern(), matcher.group(Integer.parseInt(replaceMatcher.group(1))));
		} catch (Exception e) {
			display(Constants.ADDON_PREFIX + "§cMindestens eine Capturing-Croup in \"" + this.command.get() + "\" existiert nicht in \"" + trigger.get() + "\"");
			return;
		}

		command = command.replace('§', '&');


		send(command);
	}
}
