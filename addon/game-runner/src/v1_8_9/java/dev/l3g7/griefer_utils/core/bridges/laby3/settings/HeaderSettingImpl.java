package dev.l3g7.griefer_utils.core.bridges.laby3.settings;

import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class HeaderSettingImpl extends ControlElement implements HeaderSetting {

	private static final int FONT_HEIGHT = mc().fontRendererObj.FONT_HEIGHT;

	private Integer entryHeight = null;
	private double scale = 1;

	private String name;
	private String description = null;
	private List<String> rows;

	public HeaderSettingImpl(String name) {
		super(name, null);
		this.name = name;
		this.rows = Collections.singletonList(name);
	}

	public HeaderSettingImpl(String... rows) {
		super("<multiple rows>", null);
		this.name = "<multiple rows>";
		this.rows = Arrays.asList(rows);
	}

	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		double dy = y;
		for (String row : rows) {
			LabyMod.getInstance().getDrawUtils().drawCenteredString(row, x + (maxX - x) / 2d, dy + 7, scale);
			dy += (FONT_HEIGHT + 1) * scale;
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
	public HeaderSetting scale(double scale) {
		this.scale = scale;
		return this;
	}

	@Override
	public HeaderSetting entryHeight(int height) {
		this.entryHeight = height;
		return this;
	}

	@Override
	public int getObjectWidth() {
		return 9999999; // To suppress LabyMod focusing it when clicked
	}

	@Override
	public int getEntryHeight() {
		if (entryHeight == null)
			return 22 + (rows.size() - 1) * (FONT_HEIGHT + 1);

		return entryHeight;
	}

	@Override
	public void create(Object parent) {}

}