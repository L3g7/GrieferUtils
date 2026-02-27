package dev.l3g7.griefer_utils.features.widgets.other.griefer_pass;

import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event_bus.Disableable;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.api.misc.Citybuild;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

abstract class AbstractQuest implements Disableable {

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

		EventRegisterer.register(this);
	}

	@Override
	public boolean isEnabled() {
		if (!LabyBridge.labyBridge.obfuscated() && mc().isIntegratedServerRunning())
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
	protected void increaseAmount(int amount) { setAmount(this.amount + amount); }
	protected void setAmount(int amount) {
		this.amount = amount;
	}

	public void increaseCompletions(int amount) {
		this.completions += amount;
		this.amount = isFinished() ? maxAmount : 0;
	}

	public double getProgress() {
		double progress = (double) (completions << 16) / (double) maxCompletions;
		progress += (double) amount / (double) maxAmount;
		return progress;
	}

	public void pin(Map<String, List<AbstractQuest>> pinnedQuests) {
		pinnedQuests.computeIfAbsent(getMatcher().group(), k -> new ArrayList<>()).add(this);
	}

	public void unpin(Map<String, List<AbstractQuest>> pinnedQuests) {
		List<AbstractQuest> quests = pinnedQuests.get(getMatcher().group());
		if (quests != null)
			quests.remove(this);

		EventRegisterer.unregister(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AbstractQuest quest = (AbstractQuest) o;
		return questTypeText.equals(quest.questTypeText);
	}

	@Override
	public int hashCode() {
		return Objects.hash(questTypeText);
	}

}
