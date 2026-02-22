package dev.l3g7.griefer_utils.features.widgets.griefer_pass;

import dev.l3g7.griefer_utils.core.api.event_bus.Disableable;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.api.misc.Citybuild;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;

import java.util.regex.Matcher;

abstract class AbstractQuest implements Disableable {

	private final Matcher matcher;
	private final int maxAmount;
	private int amount = 0;

	private final String formatPattern;

	protected AbstractQuest(Matcher matcher, int maxAmount) {
		this.matcher = matcher;
		this.maxAmount = maxAmount;
		EventRegisterer.register(this);

		this.formatPattern = modifyFormatPattern(Quests.computeFormatPattern(this));
	}

	protected String modifyFormatPattern(String formatPattern) {
		return formatPattern;
	}

	@Override
	public boolean isEnabled() {
		Citybuild cb = MinecraftUtil.getCurrentCitybuild();
		return cb != Citybuild.ANY && cb != Citybuild.LAVA && cb != Citybuild.WATER;
	}

	Matcher getMatcher() {
		return matcher;
	}

	public int getMaxAmount() {
		return maxAmount;
	}

	public int getAmount() {
		return amount;
	}

	public String format() {
		return String.format(formatPattern, getAmount(), getMaxAmount());
	}

	protected void increaseAmount() { increaseAmount(1); }
	protected void increaseAmount(int amount) {
		this.amount += amount;
	}

}
