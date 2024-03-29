package dev.l3g7.griefer_utils.v1_8_9.bridges.laby3.settings;

import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

public class HeaderSettingImpl extends ControlElement implements HeaderSetting {

	private static final int FONT_HEIGHT = mc().fontRendererObj.FONT_HEIGHT;

	private String name;
	private String description = null;
	private List<String> rows;

	public HeaderSettingImpl(String name) {
		super(name, null);
		this.name = name;
	}

	public HeaderSettingImpl(String... rows) {
		super("<multiple rows>", null);
		this.name = "<multiple rows>";
		this.rows = Arrays.asList(rows);
	}

	@Override
	public int getEntryHeight() {
		return super.getEntryHeight() + (rows.size() - 1) * (FONT_HEIGHT + 1);
	}

	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		for (String row : rows) {
			LabyMod.getInstance().getDrawUtils().drawCenteredString(row, x + (maxX - x) / 2d, y + 7, 1);
			y += FONT_HEIGHT + 1;
		}
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public HeaderSetting name(String name) {
		this.name = name.trim();
		this.rows = Collections.singletonList(name);
		setDisplayName(name);
		return this;
	}

	@Override
	public String getDisplayName() {
		return name;
	}

	@Override
	public HeaderSetting description(String... description) {
		this.description = String.join("\n", description).trim();
		setDescriptionText(this.description);
		return this;
	}

	@Override
	public String getDescriptionText() {
		return description;
	}

	@Override
	public void create(Object parent) {}

}