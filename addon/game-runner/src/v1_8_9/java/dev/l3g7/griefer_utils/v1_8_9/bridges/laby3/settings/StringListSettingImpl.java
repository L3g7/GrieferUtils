package dev.l3g7.griefer_utils.v1_8_9.bridges.laby3.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.api.event.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.laby3.settings.Laby3Setting;
import dev.l3g7.griefer_utils.settings.types.StringListSetting;
import net.labymod.core.LabyModCore;
import net.labymod.gui.elements.ModTextField;
import net.labymod.main.LabyMod;
import net.labymod.settings.LabyModModuleEditorGui;
import net.labymod.settings.PreviewRenderer;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.labymod.utils.ModColor;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

public class StringListSettingImpl extends ControlElement implements Laby3Setting<StringListSetting, List<String>>, StringListSetting {

	private final ExtendedStorage<List<String>> storage = new ExtendedStorage<>(list -> {
		JsonArray array = new JsonArray();
		list.forEach(s -> array.add(new JsonPrimitive(s)));
		return array;
	}, elem -> {
		List<String> list = new ArrayList<>();
		elem.getAsJsonArray().forEach(e -> list.add(e.getAsString()));
		return list;
	}, new ArrayList<>());

	public StringListSettingImpl() {
		super("§cEs gab einen Fehler!", null);
		setSettingEnabled(true);
	}

	private SettingsElement container = this;
	private StringAddSetting stringAddSetting = null;

	@Override
	public ExtendedStorage<List<String>> getStorage() {
		return storage;
	}

	@Override
	public void create(Object parent) {
		Laby3Setting.super.create(parent);
		this.container = (SettingsElement) parent;
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		drawIcon(x, y);
	}

	@Override
	public StringListSetting config(String configKey) {
		Laby3Setting.super.config(configKey);
		return initList();
	}

	public StringListSetting initList() {
		ArrayList<SettingsElement> settings = new ArrayList<>();
		for (String entry : get())
			settings.add(new StringDisplaySetting(entry));

		settings.add(stringAddSetting = new StringAddSetting());
		getSettings().remove(this);
		container.getSubSettings().addAll(settings);
		return this;
	}

	@Override
	public StringListSetting set(List<String> value) {
		Laby3Setting.super.set(value);
		getSettings().removeIf(se -> se instanceof StringDisplaySetting);

		if (getSettings().contains(stringAddSetting))
			for (String s : value)
				getSettings().add(getSettings().indexOf(stringAddSetting), new StringDisplaySetting(s));

		return this;
	}

	private List<SettingsElement> getSettings() {
		return container.getSubSettings().getElements();
	}

	private class StringDisplaySetting extends ListEntrySetting {

		private String data;

		public StringDisplaySetting(String entry) {
			super(true, true, false);
			container = StringListSettingImpl.this;
			icon(Material.PAPER);
			name(data = entry);
		}

		@Override
		protected void onChange() {
			get().remove(data);
			getSettings().remove(this);
			getStorage().callbacks.forEach(c -> c.accept(get()));
			StringListSettingImpl.this.save();
		}

		@Override
		protected void openSettings() {
			mc().displayGuiScreen(stringAddSetting.new AddStringGui(mc().currentScreen, this));
		}

		@Override
		public ExtendedStorage<List<String>> getStorage() {
			return storage;
		}

	}

	private class StringAddSetting extends EntryAddSetting {

		StringAddSetting() {
			super(StringListSettingImpl.this.displayName);
			callback(() -> mc().displayGuiScreen(new AddStringGui(mc().currentScreen, null)));
		}

		@Override
		public ExtendedStorage<List<String>> getStorage() {
			return storage;
		}

		private class AddStringGui extends GuiScreen {

			private final GuiScreen backgroundScreen;
			private final StringDisplaySetting setting;
			private ModTextField inputField;

			public AddStringGui(GuiScreen backgroundScreen, StringDisplaySetting setting) {
				this.backgroundScreen = backgroundScreen;
				this.setting = setting;
			}

			public void initGui() {
				super.initGui();
				backgroundScreen.width = width;
				backgroundScreen.height = height;
				if (backgroundScreen instanceof LabyModModuleEditorGui)
					PreviewRenderer.getInstance().init(AddStringGui.class);

				inputField = new ModTextField(0, LabyModCore.getMinecraft().getFontRenderer(), width / 2 - 150, height / 4 + 45, 300, 20);
				inputField.setFocused(true);
				inputField.setMaxStringLength(100);
				if (setting != null) {
					inputField.setText(setting.data);
					inputField.setCursorPositionEnd();
				}

				buttonList.add(new GuiButton(0, width / 2 - 105, height / 4 + 85, 100, 20, "Abbrechen"));
				buttonList.add(new GuiButton(1, width / 2 + 5, height / 4 + 85, 100, 20, setting == null ? "Hinzufügen" : "Bearbeiten"));
			}

			@Override
			public void onGuiClosed() {
				EventRegisterer.unregister(this);
			}

			public void drawScreen(int mouseX, int mouseY, float partialTicks) {
				backgroundScreen.drawScreen(0, 0, partialTicks);
				drawRect(0, 0, width, height, Integer.MIN_VALUE);

				inputField.drawTextBox();

				super.drawScreen(mouseX, mouseY, partialTicks);
			}

			public void updateScreen() {
				backgroundScreen.updateScreen();
				inputField.updateCursorCounter();
			}

			protected void actionPerformed(GuiButton button) {
				super.actionPerformed(button);
				switch (button.id) {
					case 1:
						int lastIndex = getSettings().indexOf(StringAddSetting.this);
						if (setting == null) {
							getSettings().add(lastIndex, new StringDisplaySetting(inputField.getText()));
							get().add(inputField.getText());
						} else {
							setting.name(setting.data = inputField.getText());
							int settingIndex = getSettings().indexOf(setting);
							int listIndex = get().size() - (lastIndex - settingIndex);
							get().set(listIndex, inputField.getText());
						}

						save();
						getStorage().callbacks.forEach(c -> c.accept(get()));
						// Fall-through
					case 0:
						mc().displayGuiScreen(backgroundScreen);
						backgroundScreen.initGui(); // Update settings
				}
			}

			protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
				super.mouseClicked(mouseX, mouseY, mouseButton);
				inputField.mouseClicked(mouseX, mouseY, mouseButton);
			}

			protected void keyTyped(char typedChar, int keyCode) {
				if (keyCode == 1) // ESC
					mc().displayGuiScreen(backgroundScreen);

				inputField.textboxKeyTyped(typedChar, keyCode);
			}
		}

	}

	public static abstract class EntryAddSetting extends ControlElement implements Laby3Setting<EntryAddSetting, List<String>> {

		private Runnable callback;

		public EntryAddSetting() {
			this("§cno name set");
		}

		public EntryAddSetting(String displayName) {
			super(displayName, new IconData("labymod/textures/settings/category/addons.png"));
		}

		public EntryAddSetting callback(Runnable callback) {
			this.callback = callback;
			return this;
		}

		@Override
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
			mouseOver = mouseX > x && mouseX < maxX && mouseY > y && mouseY < maxY;

			LabyMod.getInstance().getDrawUtils().drawRectangle(x, y, maxX, maxY, ModColor.toRGB(80, 80, 80, 60));
			int iconWidth = iconData != null ? 25 : 2;
			mc.getTextureManager().bindTexture(iconData.getTextureIcon());

			if (mouseOver) {
				LabyMod.getInstance().getDrawUtils().drawTexture(x + 2, y + 2, 256.0, 256.0, 18, 18);
				LabyMod.getInstance().getDrawUtils().drawString(displayName, x + iconWidth + 1, (double) y + 7 - 0);
			} else {
				LabyMod.getInstance().getDrawUtils().drawTexture(x + 3, y + 3, 256.0, 256.0, 16.0, 16.0);
				LabyMod.getInstance().getDrawUtils().drawString(displayName, x + iconWidth, (double) y + 7 - 0);
			}
		}

		@Override
		public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
			if (mouseOver)
				callback.run();
		}

	}
	public abstract static class ListEntrySetting extends ControlElement implements Laby3Setting<ListEntrySetting, List<String>> {

		private final boolean deletable, editable, movable;

		private boolean hoveringDelete = false;
		private boolean hoveringEdit = false;
		private boolean hoveringUp = false;
		private boolean hoveringDown = false;
		private boolean hasIcon;

		public SettingsElement container;

		public ListEntrySetting(boolean deletable, boolean editable, boolean movable) {
			super("§f", null);
			setSettingEnabled(false);
			this.deletable = deletable;
			this.editable = editable;
			this.movable = movable;
			hasIcon = true;
		}

		abstract protected void onChange();

		protected void openSettings() {
			throw new IllegalStateException("unimplemented");
		}

		protected void remove() {
			container.getSubSettings().getElements().remove(this);
			onChange();
			mc().currentScreen.initGui();
		}

		@Override
		public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
			super.mouseClicked(mouseX, mouseY, mouseButton);

			if (!mouseOver)
				return;

			if (hoveringEdit) {
				mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1));
				openSettings();
				return;
			}

			if (hoveringDelete) {
				mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1));
				remove();
				return;
			}

			if (!hoveringUp && !hoveringDown)
				return;

			List<SettingsElement> settings = container.getSubSettings().getElements();
			int index = settings.indexOf(this);
			settings.remove(this);
			settings.add(index + (hoveringDown ? 1 : -1), this);
			onChange();
			mc().currentScreen.initGui();
		}

		@Override
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
			hideSubListButton();
			super.draw(x, y, maxX, maxY, mouseX, mouseY);
			if (hasIcon)
				drawIcon(x, y);

			mouseOver = mouseX > x && mouseX < maxX && mouseY > y && mouseY < maxY;
			if (!mouseOver)
				return;

			int xPosition = maxX - 20;
			double yPosition = y + 4.5;

			if (deletable) {
				hoveringDelete = mouseX >= xPosition && mouseY >= yPosition && mouseX <= xPosition + 15.5 && mouseY <= yPosition + 16;

				mc.getTextureManager().bindTexture(new ResourceLocation("labymod/textures/misc/blocked.png"));
				LabyMod.getInstance().getDrawUtils().drawTexture(maxX - (hoveringDelete ? 20 : 19), y + (hoveringDelete ? 3.5 : 4.5), 256, 256, hoveringDelete ? 16 : 14, hoveringDelete ? 16 : 14);
			}

			if (editable) {
				xPosition -= 20;
				hoveringEdit = mouseX >= xPosition && mouseY >= yPosition && mouseX <= xPosition + 15.5 && mouseY <= yPosition + 16;

				mc.getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/pencil.png"));
				LabyMod.getInstance().getDrawUtils().drawTexture(maxX - (hoveringEdit ? 40 : 39), y + (hoveringEdit ? 3.5 : 4.5), 256, 256, hoveringEdit ? 16 : 14, hoveringEdit ? 16 : 14);
			}

			if (!movable)
				return;

			mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/server_selection.png"));

			List<SettingsElement> allSettings = container.getSubSettings().getElements();
			List<SettingsElement> settings = allSettings.stream()
				.filter(s -> getClass().isInstance(s))
				.collect(Collectors.toList());

			int index = settings.indexOf(this);
			hoveringUp = index != 0;

			// Check if the button should exist at all
			xPosition -= 19;
			yPosition = y + 1.5;
			if (hoveringUp) {
				hoveringUp = mouseX >= xPosition && mouseY >= yPosition && mouseX <= xPosition + 44 / 3d && mouseY <= yPosition + 28 / 3d;
				LabyMod.getInstance().getDrawUtils().drawTexture(maxX - 59, y + 1.5, 99, hoveringUp ? 37 : 5, 14, 7, 14 / 0.75d, 7 / 0.75d);
			}

			// Check if the button should exist at all
			hoveringDown = index != settings.size() - 1;
			yPosition += 11;
			if (hoveringDown) {
				hoveringDown = mouseX >= xPosition && mouseY >= yPosition && mouseX <= xPosition + 44 / 3d && mouseY <= yPosition + 28 / 3d;
				LabyMod.getInstance().getDrawUtils().drawTexture(maxX - 59, y + 12.5, 67, hoveringDown ? 52 : 20, 14, 7, 14 / 0.75d, 7 / 0.75d);
			}
		}

	}


	@Override
	public StringListSetting placeholder(String placeholder) {
		return null;
	}

	@Override
	public StringListSetting entryIcon(Object icon) {
		return null;
	}
}
