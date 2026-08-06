package dev.l3g7.griefer_utils.features.widgets.other.griefer_pass;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event_bus.Disableable;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.misc.griefer_games.Citybuild;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.regex.Matcher;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

abstract class AbstractQuest implements Disableable, Comparable<AbstractQuest> {

	public boolean isShadowed = false;
	public int index;

	private String displayText;
	private String questTypeText;
	private String questText;
	private Matcher matcher;
	private int maxAmount;
	public int maxCompletions;
	private boolean approximate;

	private int amount = 0;
	public int completions = 0;

	// Avoid using a constructor since it cascades into every impl
	public void init(int index, Matcher matcher, String displayText, boolean approximate, int maxAmount, int maxCompletions) {
		this.index = index;
		this.questText = matcher.group();
		this.questTypeText = this.questText.replaceAll("\\d", "");
		this.displayText = displayText;
		this.approximate = approximate;
		this.matcher = matcher;
		this.maxAmount = maxAmount;
		this.maxCompletions = maxCompletions;
	}

	@Override
	public boolean isEnabled() {
		if (isShadowed)
			return false;

		if (!LabyBridge.labyBridge.inDevEnv() && mc().isIntegratedServerRunning())
			return true;

		Citybuild cb = MinecraftUtil.getCurrentCitybuild();
		return cb != Citybuild.ANY && cb != Citybuild.LAVA && cb != Citybuild.WATER;
	}

	Matcher getMatcher() {
		return matcher;
	}

	public String getDisplayText() {
		return displayText;
	}

	public int getAmount() {
		return amount;
	}

	public Pair<String, String> format() {
		String progress;
		if (isFinished()) {
			progress = String.format("§a%d/%d", amount, maxAmount);
			if (maxCompletions != 1)
				progress += String.format(" %d/%d", completions, maxCompletions);

			return new Pair<>("§m" + displayText, progress);
		}

		progress = String.format(approximate ? "§e~%d§7/§f%d" : "§e%d§7/§f%d", amount, maxAmount);
		if (maxCompletions != 1)
			progress += String.format(" §a%d§7/§f%d", completions, maxCompletions);

		return new Pair<>(displayText, progress);
	}

	public boolean isFinished() {
		return completions == maxCompletions;
	}

	protected void increaseAmount() { increaseAmount(1); }
	protected void increaseAmount(int amount) { setAmount(this.amount + amount, true); }
	protected void setAmount(int amount, boolean updateConfig) {
		int previousAmount = this.amount;
		this.amount = Math.min(amount, maxAmount);

		if (updateConfig && previousAmount != this.amount)
			FileProvider.getSingleton(GrieferPass.class).onQuestUpdate(false);
	}

	public void increaseCompletions(int amount) {
		this.completions += Math.min(amount, maxCompletions);
		this.amount = isFinished() ? maxAmount : 0;
	}

	public double getProgress() {
		double progress = (double) (completions << 16) / (double) maxCompletions;
		progress += (double) amount / (double) maxAmount;
		return progress;
	}

	public void pin(Map<String, TreeSet<AbstractQuest>> questLookup, Map<String, TreeSet<AbstractQuest>> questTypeLookup) {
		questLookup.computeIfAbsent(questText, k -> new TreeSet<>()).add(this);
		questTypeLookup.computeIfAbsent(questTypeText, k -> new TreeSet<>()).add(this);

		EventRegisterer.register(this);
	}

	public void unpin(Map<String, TreeSet<AbstractQuest>> questLookup, Map<String, TreeSet<AbstractQuest>> questTypeLookup) {
		EventRegisterer.unregister(this);

		TreeSet<AbstractQuest> set = questLookup.get(questText);
		if (set == null || !set.remove(this))
			return;

		TreeSet<AbstractQuest> quests = questTypeLookup.get(questTypeText);
		if (quests != null)
			quests.remove(this);
	}

	public void updateShadowing(Map<String, TreeSet<AbstractQuest>> questTypeLookup) {
		if (isFinished()) {
			isShadowed = true;
			return;
		}

		for (AbstractQuest quest : questTypeLookup.get(questTypeText)) {
			if (quest.isFinished())
				continue;

			isShadowed = quest.index != index; // isShadowed = !first quest is this
			return;
		}
	}

	public JsonObject serialize() {
		JsonObject obj = new JsonObject();
		obj.addProperty("index", index);
		obj.addProperty("text", questText);
		obj.addProperty("amount", amount);
		obj.addProperty("max_amount", maxAmount);
		obj.addProperty("completions", completions);
		obj.addProperty("max_completions", maxCompletions);
		return obj;
	}

	public static AbstractQuest deserialize(JsonObject obj) {
		AbstractQuest quest = Quests.parseQuest(
			obj.get("index").getAsInt(),
			obj.get("text").getAsString(),
			obj.get("amount").getAsInt(),
			obj.get("max_amount").getAsInt(),
			obj.get("completions").getAsInt(),
			obj.get("max_completions").getAsInt()
		);

		if (quest instanceof VisitBiomesQuest vbq)
			vbq.deserializeBiomes(obj);

		return quest;
	}

	@Override
	public int compareTo(@NotNull AbstractQuest o) {
		return Integer.compare(index, o.index);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AbstractQuest quest = (AbstractQuest) o;
		return questTypeText.equals(quest.questTypeText) && (isFinished() == quest.isFinished());
	}

	@Override
	public int hashCode() {
		return Objects.hash(questTypeText, isFinished());
	}

}
