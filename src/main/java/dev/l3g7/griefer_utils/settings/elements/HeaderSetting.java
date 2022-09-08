package dev.l3g7.griefer_utils.settings.elements;

import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.settings.elements.HeaderElement;

public class HeaderSetting extends HeaderElement implements ElementBuilder<HeaderSetting> {

	private int entryHeight = super.getEntryHeight();

	public HeaderSetting() {
		super("§c");
	}

	public HeaderSetting(String name) {
		super(name);
	}

	public HeaderSetting scale(double scale) {
		Reflection.set(this, "textSize", scale);
		return this;
	}

	public HeaderSetting entryHeight(int entryHeight) {
		this.entryHeight = entryHeight;
		return this;
	}

	@Override
	public int getEntryHeight() {
		return entryHeight;
	}

}
