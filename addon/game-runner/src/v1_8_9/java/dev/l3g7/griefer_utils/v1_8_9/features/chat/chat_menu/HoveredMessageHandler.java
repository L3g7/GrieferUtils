package dev.l3g7.griefer_utils.v1_8_9.features.chat.chat_menu;

import dev.l3g7.griefer_utils.api.event.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.api.misc.functions.Supplier;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import net.labymod.api.client.chat.advanced.ChatMessagesWidget;
import net.labymod.api.client.chat.advanced.IngameChatTab;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.gui.screen.activity.activities.ingame.chat.WindowAccessor;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.render.font.ComponentRenderMeta;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.configuration.labymod.chat.AdvancedChatMessage;
import net.labymod.api.configuration.labymod.chat.ChatTab;
import net.labymod.api.configuration.labymod.chat.ChatWindow;
import net.labymod.api.configuration.labymod.chat.config.ChatWindowConfig;
import net.labymod.api.configuration.labymod.chat.config.RootChatTabConfig;
import net.labymod.api.metadata.Metadata;
import net.labymod.core.main.LabyMod;
import net.minecraft.util.IChatComponent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

class HoveredMessageHandler {

	private static AdvancedChatMessage renderedMessage = null;
	private static ComponentRenderMeta lastHoveredRenderMeta = null;
	private static Supplier<ComponentRenderMeta> lastHoveredRenderMetaSupplier = null;
	private static AdvancedChatMessage hoveredMessage = null;

	public static IChatComponent getMessage() {
		if (hoveredMessage == null)
			return null;

		return (IChatComponent) hoveredMessage.component();
	}

	public static IChatComponent getOriginalMessage() {
		if (hoveredMessage == null)
			return null;

		return (IChatComponent) hoveredMessage.originalComponent();
	}

	@OnEnable
	private static void holyFuckThisClassShouldntBeNeeded() {
		HashSet<ChatWindow> windows = Reflection.get(LabyMod.references().advancedChatController(), "windows");
		for (ChatWindow chatWindow : new HashSet<>(windows)) {
			windows.remove(chatWindow);
			windows.add(new ChatWindowWrapper(chatWindow));
		}
	}

	private static class ChatWindowWrapper implements ChatWindow {

		private final ChatWindow defaultChatWindow;

		public ChatWindowWrapper(ChatWindow defaultChatWindow) {
			this.defaultChatWindow = defaultChatWindow;
		}

		public ChatTab getActiveTab() { return new ChatTabWrapper(defaultChatWindow.getActiveTab()); }

		public ChatWindowConfig config() { return defaultChatWindow.config(); }
		public List<ChatTab> getTabs() { return defaultChatWindow.getTabs(); }
		public void save() { defaultChatWindow.save(); }
		public void switchToTab(ChatTab chatTab) { defaultChatWindow.switchToTab(chatTab); }
		public void deleteTab(ChatTab chatTab) { defaultChatWindow.deleteTab(chatTab); }
		public ChatTab initializeTab(RootChatTabConfig config, ChatTab template, boolean sort) { return defaultChatWindow.initializeTab(config, template, sort); }
		public ChatTab initializeTab(RootChatTabConfig config) { return defaultChatWindow.initializeTab(config); }
		public boolean isMainWindow() { return defaultChatWindow.isMainWindow(); }
		public <T extends ChatTab> boolean forEachChatTab(Class<T> clazz, Predicate<T> predicate) { return defaultChatWindow.forEachChatTab(clazz, predicate); }
		public boolean forEachIngameChatTab(Predicate<IngameChatTab> predicate) { return defaultChatWindow.forEachIngameChatTab(predicate); }
		public ChatTab getFirstChatTabWithType(RootChatTabConfig.Type type) { return defaultChatWindow.getFirstChatTabWithType(type); }
	}

	private static class ChatTabWrapper extends ChatTab {

		private final ChatTab chatTab;

		private ChatTabWrapper(ChatTab chatTab) {
			super(chatTab.window(), chatTab.rootConfig());
			this.chatTab = chatTab;
		}

		public @NotNull Widget createContentWidget(WindowAccessor window) {
			if (chatTab instanceof IngameChatTab ict)
				return new GrieferUtilsChatMessagesWidget(ict, window);

			return chatTab.createContentWidget(window);
		}

		public @NotNull String getName() { return chatTab.getName(); }
		public @NotNull String getCustomName() { return chatTab.getCustomName(); }
		public void metadata(Metadata metadata) { chatTab.metadata(metadata); }
		public Metadata metadata() { return chatTab.metadata(); }
		public int getUnread() { return chatTab.getUnread(); }
		public void resetUnread() { chatTab.resetUnread(); }
		public void invalidateCache() { chatTab.invalidateCache(); }
		public void copy(@NotNull ChatTab ct) { chatTab.copy(ct); }


	}

	private static class GrieferUtilsChatMessagesWidget extends ChatMessagesWidget {

		public GrieferUtilsChatMessagesWidget(IngameChatTab tab, WindowAccessor window) {
			super(tab, window);

			if (!(tab.getMessages() instanceof DetectingList))
				Reflection.set(tab, "messages", new DetectingList(tab.getMessages()));
		}

		@Override
		public void renderWidget(Stack stack, MutableMouse mouse, float tickDelta) {
			lastHoveredRenderMetaSupplier = () -> Reflection.get(this, "lastHoveredComponentMeta");
			renderedMessage = null;
			lastHoveredRenderMeta = null;
			super.renderWidget(stack, mouse, tickDelta);
			lastHoveredRenderMetaSupplier = null;
		}

	}

	private static class DetectingList extends ArrayList<AdvancedChatMessage> {

		public DetectingList(Collection<AdvancedChatMessage> c) {
			super(c);
		}

		@NotNull
		public Iterator<AdvancedChatMessage> iterator() {
			if (lastHoveredRenderMetaSupplier == null)
				return super.iterator();

			return new DetectingIterator(super.iterator());
		}

		private static class DetectingIterator implements Iterator<AdvancedChatMessage> {

			private final Iterator<AdvancedChatMessage> iterator;

			private DetectingIterator(Iterator<AdvancedChatMessage> iterator) {
				this.iterator = iterator;
			}

			public AdvancedChatMessage next() {
				return renderedMessage = iterator.next();
			}

			public boolean hasNext() {
				ComponentRenderMeta currentHoveredMessage = lastHoveredRenderMetaSupplier.get();
				if (currentHoveredMessage != null
					&& lastHoveredRenderMeta != currentHoveredMessage
					&& currentHoveredMessage.getHovered().isPresent()) {
					hoveredMessage = renderedMessage;
					lastHoveredRenderMeta = currentHoveredMessage;
				}

				return iterator.hasNext();
			}

			public void remove() { iterator.remove(); }
			public void forEachRemaining(Consumer<? super AdvancedChatMessage> action) { iterator.forEachRemaining(action); }
		}

	}

}
