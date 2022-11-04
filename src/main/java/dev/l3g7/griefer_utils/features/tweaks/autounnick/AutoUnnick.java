package dev.l3g7.griefer_utils.features.tweaks.autounnick;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.event_bus.EventPriority;
import dev.l3g7.griefer_utils.event.events.chat.MessageModifyEvent;
import dev.l3g7.griefer_utils.event.events.chat.MessageSendEvent;
import dev.l3g7.griefer_utils.event.events.network.tablist.TabListNameUpdateEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.NameCache;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.event.events.network.tablist.TabListEvent.updatePlayerInfoList;
import static dev.l3g7.griefer_utils.misc.Constants.MESSAGE_PATTERNS;

@Singleton
public class AutoUnnick extends Feature {

	private static final String COMMAND = "/grieferutils_autounnick_namehistory_prompt_fix ";

	private final BooleanSetting tab = new BooleanSetting()
			.name("In Tabliste")
			.description("Ob Spieler in der Tabliste entnickt werden sollen.")
			.config("tweaks.auto_unnick.tab")
			.icon("tab_list")
			.callback(c -> updatePlayerInfoList())
			.defaultValue(false);

	private final BooleanSetting enabled = new BooleanSetting()
			.name("Automatisch entnicken")
			.description("Entnickt automatisch Spieler.")
			.config("tweaks.auto_unnick.active")
			.icon(Material.NAME_TAG)
			.defaultValue(true)
			.callback(c -> updatePlayerInfoList())
			.subSettingsWithHeader("Automatisch entnicken", tab);

	public AutoUnnick() {
		super(Category.TWEAK);
		Category.TWEAK.setting.callback(c -> updatePlayerInfoList());
	}

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	@EventListener
	public void onMessageSend(MessageSendEvent event) {
		if (event.getMsg().startsWith(COMMAND)) {
			suggest(event.getMsg().substring(COMMAND.length()));
			event.setCanceled(true);
		}
	}

	@EventListener(priority = EventPriority.HIGH)
	public void onTabListNameUpdate(TabListNameUpdateEvent event) {
		if (!isActive() || !isOnGrieferGames() || !tab.get())
			return;

		String text = event.getComponent().getUnformattedText();

		if (!text.contains("~"))
			return;

		String nickName = text.substring(text.indexOf('~'));
		String[] parts = event.getComponent().getFormattedText().split(" §r§8\u2503 §r");
		setNameWithPrefix(event.getComponent(), parts[0], parts[1], nickName, true);
	}

	@EventListener
	public void onMessageModifyChat(MessageModifyEvent event) {
		if (!isActive() || !isOnGrieferGames())
			return;

		String text = event.getMessage().getUnformattedText();

		if (!text.contains("\u2503") || !text.contains("~"))
			return;

		String name = text.substring(text.indexOf('\u2503') + 2);
		int bracketIndex = name.indexOf(']') == -1 ? Integer.MAX_VALUE : name.indexOf(']');
		int spaceIndex = name.indexOf(' ');

		if (spaceIndex == -1 && bracketIndex == Integer.MAX_VALUE)
			return;

		name = name.substring(0, Math.min(spaceIndex, bracketIndex));

		if (!name.contains("~"))
			return;

		for (Pattern pattern : MESSAGE_PATTERNS) {
			Matcher matcher = pattern.matcher(event.getMessage().getFormattedText());

			if (matcher.matches()) {
				event.setMessage(setNameWithPrefix(event.getMessage(), matcher.group("rank"), matcher.group("name"), name, false));
				return;
			}
		}
	}

	private IChatComponent setNameWithPrefix(IChatComponent iChatComponent, String rank, String formattedName, String unformattedName, boolean isTabList) {
		List<IChatComponent> everything = iChatComponent.getSiblings();
		IChatComponent parent = everything.get(everything.size() - 1);

		if (parent.getSiblings().isEmpty())
			parent = iChatComponent;

		List<IChatComponent> lastSiblings = parent.getSiblings();

		int playerIndex = -1;

		for (ListIterator<IChatComponent> iterator = lastSiblings.listIterator(); iterator.hasNext(); ) {
			String text = iterator.next().getUnformattedTextForChat();

			if (playerIndex == -1) {
				if (text.equals("\u2503 "))
					playerIndex = iterator.nextIndex();
				continue;
			}

			if (text.contains(" ") || text.contains("]"))
				break;
			else
				iterator.remove();
		}

		String prefix = new PrefixFinder(rank, formattedName).getPrefix();

		Collection<IChatComponent> name = getNameComponents(NameCache.getName(unformattedName), prefix, isTabList);
		ClickEvent clickEvent = parent.getChatStyle().getChatClickEvent();

		ChatComponentText nickName = new ChatComponentText("");
		getNameComponents(unformattedName, prefix, false).forEach(nickName::appendSibling);

		// Add the HoverEvent and make it italic
		for (IChatComponent component : name) {
			component.getChatStyle()
					.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, nickName))
					.setItalic(true);

			if (clickEvent != null)
				component.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, COMMAND + clickEvent.getValue()));
		}

		parent.getChatStyle().setChatClickEvent(clickEvent);

		lastSiblings.addAll(playerIndex, name);

		return iChatComponent;
	}

	private Collection<IChatComponent> getNameComponents(String name, String prefix, boolean isTabList) {

		if (name == null)
			return ImmutableList.of(new ChatComponentText("404").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_RED)));

		if (prefix.length() <= 2) {
			ChatStyle style = new ChatStyle()
					.setColor(EnumChatFormatting.func_175744_a(Integer.parseInt(String.valueOf(prefix.charAt(0)), 16)));
			if (prefix.contains("l") && !isTabList)
				style.setBold(true);
			if (prefix.contains("k"))
				style.setObfuscated(true);

			return ImmutableList.of(new ChatComponentText(name).setChatStyle(style));
		}

		List<IChatComponent> components = new ArrayList<>();
		char[] chars = prefix.toCharArray();

		for (int i = 0; i < name.toCharArray().length; i++) {

			ChatStyle style = new ChatStyle()
					.setColor(EnumChatFormatting.func_175744_a(Integer.parseInt(String.valueOf(chars[i % chars.length]), 16)));

			if (!isTabList)
				style.setBold(true);

			String text = String.valueOf(name.charAt(i));
			if (chars[i % chars.length] == chars[(i + 1) % chars.length] && name.length() != i + 1)
				text += name.charAt(++i);

			components.add(new ChatComponentText(text).setChatStyle(style));
		}

		return components;
	}
}
