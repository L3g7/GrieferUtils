package dev.l3g7.griefer_utils.features.widgets.other.griefer_pass;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Function;
import dev.l3g7.griefer_utils.core.events.GuiModifyItemsEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.WindowClickEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.widgets.Laby3Widget;
import dev.l3g7.griefer_utils.features.widgets.Widget.ComplexWidget;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

@Singleton
public class GrieferPass extends ComplexWidget {

	private static final Pattern COMPLETE_PATTERN = Pattern.compile("^\\[GrieferPass] Du hast die Aufgabe (.+) abgeschlossen\\.$");
	private static final ItemStack ADD_ALL = ItemUtil.createItem(new ItemStack(Items.gold_ingot), false, "§6Alle Aufgaben anpinnen");
	private static final ItemStack REMOVE_ALL = ItemUtil.createItem(new ItemStack(Items.gold_ingot), true, "§6Alle Aufgaben entpinnen");

	private int lastIndex = -1;
	private final Map<String, TreeSet<AbstractQuest>> questLookup = new HashMap<>();
	private final Map<String, TreeSet<AbstractQuest>> questTypeLookup = new HashMap<>();

	private final Set<Integer> failedQuests = new HashSet<>();

	private final DropDownSetting<Sorting> sorting = DropDownSetting.create(Sorting.class)
		.name("Sortierung")
		.description("In welcher Reihenfolge GrieferPass-Aufgaben angezeigt werden sollen.")
		.icon("command_suggestions")
		.defaultValue(Sorting.PROGRESS);

	private final SwitchSetting removeFinished = SwitchSetting.create()
		.name("Fertige Aufgaben entfernen")
		.description("Ob abgeschlossene GrieferPass-Aufgaben automatisch entpinnt werden sollen.")
		.icon("trophy");

	private final SwitchSetting ignoreCaseOpening = SwitchSetting.create()
		.name("CaseOpening-Aufgaben ignorieren")
		.description("Ob die CaseOpening-Aufgaben beim Hinzufügen aller Aufgaben ignoriert werden sollen.")
		.icon("chest_golden")
		.defaultValue(true);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("GrieferPass")
		.description("Zeigt dir angepinnte GrieferPass-Aufgaben.")
		.icon("fancy_book")
		.subSettings(sorting, removeFinished, ignoreCaseOpening)
		.since("2.4-BETA-1");

	void onQuestUpdate(boolean updateShadowing) {
		JsonArray array = new JsonArray();
		streamQuests().forEach(q -> {
			if (updateShadowing)
				q.updateShadowing(questTypeLookup);
			array.add(q.serialize());
		});
		Config.set("modules.griefer_pass.quests." + uuid(), array);
		Config.set("modules.griefer_pass.next_reset." + uuid(), new JsonPrimitive(getNextServerRestart()));
		Config.save();
	}

	@EventListener
	private void loadQuests(GrieferGamesJoinEvent event) {
		String key = "modules.griefer_pass.quests." + uuid();
		if (!Config.has(key))
			return;

		for (JsonElement quest : Config.get(key).getAsJsonArray())
			AbstractQuest.deserialize(quest.getAsJsonObject()).pin(questLookup, questTypeLookup);

		streamQuests().forEach(q -> q.updateShadowing(questTypeLookup));

		JsonElement nextReset = Config.get("modules.griefer_pass.next_reset." + uuid());
		if (nextReset == null)
			return;

		new Timer().schedule(new TimerTask() {
			public void run() {
				TickScheduler.runNextRenderTick(() -> {
					streamQuests().filter(q -> q.index / 100 == 0).forEach(q -> q.unpin(questLookup, questTypeLookup));
				});
			}
		}, new Date(nextReset.getAsLong()), 24 * 3600 * 1000);
	}

	@EventListener(triggerWhenDisabled = true)
	private void onMessageReceive(MessageReceiveEvent event) {
		Matcher matcher = COMPLETE_PATTERN.matcher(event.message.getUnformattedText());
		if (!matcher.matches())
			return;

		String completedQuestName = matcher.group(1);
		TreeSet<AbstractQuest> quests = questLookup.get(completedQuestName);
		if (quests == null)
			return;

		for (AbstractQuest quest : quests) {
			if (!quest.isFinished()) {
				quest.increaseCompletions(1);
				break;
			}
		}
	}

	@EventListener
	private void onGuiModify(GuiModifyItemsEvent event) {
		String title = event.getTitle();
		boolean isDaily = title.startsWith("§6Pass-Aufgaben");
		if (!isDaily && !title.startsWith("§6Wöchentliche Aufgaben §7- §0Woche "))
			return;

		lastIndex = isDaily ? 0 : Integer.parseInt(title.substring("§6Wöchentliche Aufgaben §7- §0Woche ".length()).replaceAll("§.", ""));
		boolean isAnyPinned = streamQuests().anyMatch(q -> q.index / 100 == lastIndex);

		String quests = lastIndex == 0 ? "täglichen Aufgaben" : "Aufgaben der Woche " + lastIndex;
		ItemUtil.setLore(ADD_ALL, "§7Pinnt alle " + quests + " in GrieferUtils an.");
		ItemUtil.setLore(REMOVE_ALL, "§7Entpinnt alle " + quests + " in GrieferUtils.");

		if (isAnyPinned)
			event.setItem(46, REMOVE_ALL);
		else
			event.setItem(46, ADD_ALL);

		for (Pair<Integer, ItemStack> questStack : getQuestStacks(event::getItem)) {
			int questIndex = lastIndex * 100 + questStack.a;
			Optional<AbstractQuest> pinnedQuest = streamQuests().filter(q -> q.index == questIndex).findAny();

			List<String> lore = ItemUtil.getLore(questStack.b);
			if (lore.get(lore.size() - 2).startsWith("§fGrieferUtils")) {
				lore = lore.subList(0, lore.size() - 3);
				pinnedQuest.ifPresent(quest -> {
					// Update amount
					AbstractQuest parsed = parseQuest(questIndex, questStack.b);
					if (parsed != null)
						quest.setAmount(parsed.getAmount(), true);
				});
			}

			lore.add("");
			lore.add("§fGrieferUtils: " + (pinnedQuest.isPresent() ? "§aAngepinnt" : "§cNicht angepinnt"));
			lore.add("§7Klicke, um diese Quest " + (pinnedQuest.isPresent() ? "zu entpinnen." : "anzupinnen."));
			ItemUtil.setLore(questStack.b, lore);
			questStack.b.getTagCompound().setInteger("griefer_utils_quest_index", questIndex);
		}
	}

	@EventListener
	private void onWindowClick(WindowClickEvent event) {
		if (event.itemStack == null || !event.itemStack.hasTagCompound())
			return;

		// Manual add / remove
		if (event.itemStack.getTagCompound().hasKey("griefer_utils_quest_index", 3 /* int */)) {
			int questIndex = event.itemStack.getTagCompound().getInteger("griefer_utils_quest_index");
			boolean isPinned = streamQuests().anyMatch(q -> q.index == questIndex);
			AbstractQuest quest = parseQuest(questIndex, event.itemStack);
			if (quest == null)
				return;

			if (isPinned)
				quest.unpin(questLookup, questTypeLookup);
			else
				quest.pin(questLookup, questTypeLookup);
			onQuestUpdate(true);
			return;
		}

		// Add / Remove all
		boolean isAdd = event.itemStack == ADD_ALL;
		if (!isAdd && event.itemStack != REMOVE_ALL)
			return;

		Container container = player().openContainer;

		for (Pair<Integer, ItemStack> questStack : getQuestStacks(slot -> container.getSlot(slot).getStack())) {
			AbstractQuest quest = parseQuest(lastIndex * 100 + questStack.a, questStack.b);
			if (quest == null)
				continue;

			if (isAdd) {
				if (!ignoreCaseOpening.get() || !Quests.DISPLAY_PATTERNS.get(quest.getMatcher().pattern()).equals("Öffne {TARGET} Kisten"))
					quest.pin(questLookup, questTypeLookup);
			} else {
				quest.unpin(questLookup, questTypeLookup);
			}
		}

		onQuestUpdate(true);
	}

	private Iterable<Pair<Integer, ItemStack>> getQuestStacks(Function<Integer, ItemStack> stackFn) {
		return () -> new Iterator<>() {
			int nextOffset = 0;
			ItemStack nextItem = null;

			@Override
			public boolean hasNext() {
				int nextSlot = lastIndex == 0 ? nextOffset + 37 /* dailies */ : (nextOffset / 7) * 9 + (nextOffset % 7) + 10 /* weeklies */;
				return (nextItem = stackFn.apply(nextSlot)) != null && nextItem.getItem() != Item.getItemFromBlock(Blocks.stained_glass_pane);
			}

			@Override
			public Pair<Integer, ItemStack> next() {
				return new Pair<>(nextOffset++, nextItem);
			}
		};
	}

	private AbstractQuest parseQuest(int questIndex, ItemStack stack) {
		List<String> lore = ItemUtil.getLore(stack);
		String amountLine = lore.get(0).replaceAll("§.", "");
		String progressLine = lore.get(1).replaceAll("§.", "");

		if (!amountLine.startsWith("Fortschritt: "))
			return null; // Cursed shit™ by GG

		String[] amounts = amountLine.substring("Fortschritt: ".length()).split("/");
		int amount = Integer.parseInt(amounts[0]);
		int maxAmount = Integer.parseInt(amounts[1]);

		int completions = lore.get(lore.size() - 4).startsWith("§aAufgabe abgeschlossen") ? 1 : 0;
		int maxCompletions = 1;
		if (progressLine.startsWith("Abgeschlossen: ")) {
			String[] completionParts = progressLine.substring("Abgeschlossen: ".length()).split("/");
			completions = Integer.parseInt(completionParts[0]);
			maxCompletions = Integer.parseInt(completionParts[1]);
		}

		String questText = stack.getDisplayName().replaceAll("§.", "");
		AbstractQuest quest = Quests.parseQuest(
			questIndex,
			questText,
			amount,
			maxAmount,
			completions,
			maxCompletions
		);

		if (quest != null)
			return quest;

		if (!failedQuests.add(questIndex))
			return null;

		labyBridge.notify("§eUnbekannte Quest", "§eDie Quest \"" + questText + "\" wurde noch nicht implementiert.");
		return null;
	}

	private Stream<AbstractQuest> streamQuests() {
		return questTypeLookup.values().stream().flatMap(Collection::stream);
	}

	@Override
	public KVPair[] getLines() {
		List<Pair<String, String>> lines = new ArrayList<>();

		AtomicInteger totalCompletions = new AtomicInteger();
		AtomicInteger maxTotalCompletions = new AtomicInteger();

		if (removeFinished.get()) {
			for (AbstractQuest quest : streamQuests().collect(Collectors.toList()))
				if (quest.isFinished())
					quest.unpin(questLookup, questTypeLookup);
		}

		Map<AbstractQuest, AtomicInteger> totalQuests = new HashMap<>();
		for (TreeSet<AbstractQuest> quests : questTypeLookup.values()) {
			for (AbstractQuest quest : quests) {
				totalQuests.computeIfAbsent(quest, k -> new AtomicInteger()).incrementAndGet();
			}
		}

		totalQuests.entrySet().stream()
			.sorted(Map.Entry.comparingByKey(sorting.get().comparator.thenComparing(Sorting.TIME.comparator)))
			.forEachOrdered(e -> {
				totalCompletions.addAndGet(e.getKey().completions);
				maxTotalCompletions.addAndGet(e.getKey().maxCompletions);

				Pair<String, String> line = e.getKey().format();
				if (e.getValue().get() > 1)
					line.b += String.format(" (+ %d)", e.getValue().get() - 1);
				lines.add(line);
			});

		lines.add(0, new Pair<>("GrieferPass", "§a" + totalCompletions + "§7/§f" + maxTotalCompletions));
		return lines.stream()
			.map(e -> new KVPair( new ChatComponentText(e.a), new ChatComponentText(e.b) ))
			.toArray(KVPair[]::new);
	}

	@Override
	protected LabyWidget getLaby3() {
		return new GrieferPassL3();
	}

	private class GrieferPassL3 extends Laby3Widget.ComplexLaby3Widget {
		public GrieferPassL3() {
			super(GrieferPass.this);
		}

		@Override
		public String getComparisonName() {
			String pkg = getClass().getPackage().getName();
			pkg = pkg.substring(0, pkg.lastIndexOf("."));
			return pkg + "." + getControlName();
		}
	}

	enum Sorting implements Named {
		ALPHABETICAL("Alphabetisch", Comparator.comparing(AbstractQuest::getDisplayText)),
		TIME("Zeitlich", Comparator.comparingInt(q -> q.index)),
		PROGRESS("Fortschritt", Comparator.comparing(AbstractQuest::getProgress).reversed() /* reversed to descending progress */);

		private final String name;
		private final Comparator<AbstractQuest> comparator;

		Sorting(String name, Comparator<AbstractQuest> comparator) {
			this.name = name;
			this.comparator = comparator;
		}

		@Override
		public String getName() {
			return name;
		}

	}

}
