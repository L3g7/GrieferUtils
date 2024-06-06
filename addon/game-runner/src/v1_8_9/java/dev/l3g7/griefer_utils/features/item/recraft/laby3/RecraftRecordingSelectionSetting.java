package dev.l3g7.griefer_utils.features.item.recraft.laby3;

import com.google.gson.JsonNull;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.labymod.laby3.settings.Laby3Setting;
import dev.l3g7.griefer_utils.core.settings.types.CategorySetting;
import dev.l3g7.griefer_utils.features.item.recraft.laby3.RecraftRecording.RecordingDisplaySetting;
import dev.l3g7.griefer_utils.features.item.recraft.laby3.RecraftRecording.RecordingMode;
import dev.l3g7.griefer_utils.features.item.recraft.laby3.crafter.CraftPlayer;
import dev.l3g7.griefer_utils.features.item.recraft.laby3.decompressor.DecompressPlayer;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.util.render.GlEngine;
import net.labymod.main.LabyMod;
import net.labymod.main.ModTextures;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.l3g7.griefer_utils.features.item.recraft.laby3.Recraft.getSubSettingsOfType;
import static dev.l3g7.griefer_utils.features.item.recraft.laby3.Recraft.iterate;
import static dev.l3g7.griefer_utils.features.item.recraft.laby3.RecraftRecording.RecordingMode.*;

public class RecraftRecordingSelectionSetting extends SmallButtonSetting implements Laby3Setting<RecraftRecordingSelectionSetting, Object> {

	private final ExtendedStorage<Object> storage = new ExtendedStorage<>(e -> JsonNull.INSTANCE, e -> NULL, NULL);
	private RecraftRecording recording;
	private final RecraftRecording container;

	public ArrayList<SettingsElement> path() { return Reflection.get(mc.currentScreen, "path"); }

	public RecraftRecordingSelectionSetting(RecraftRecording container) {
		super(new IconData(Material.BARRIER));
		this.container = container;
		setSettingEnabled(true);
		setDisplayName("Aufzeichnung auswählen");
//		subSettings();
		callback(this::createSettings);
		setSelectedRecording(null);
	}

	private void createSettings() {
		List<SettingsElement> settings = getSubSettings().getElements();
		while (settings.size() > 4)
			settings.remove(4);

		List<RecraftPageSetting> pages = getSubSettingsOfType((SettingsElement) FileProvider.getSingleton(Recraft.class).getMainElement(), RecraftPageSetting.class);

		settings.add(new RecordingSelectionSetting(null));

		for (RecraftPageSetting page : pages) {
			CategorySetting category = CategorySetting.create()
				.name(page.name.get())
				.icon(Material.EMPTY_MAP);

			List<RecordingDisplaySetting> displays = getSubSettingsOfType(page, RecordingDisplaySetting.class);
			if (container.mode.get() == DECOMPRESS)
				displays.remove(container.mainSetting);

			if (displays.isEmpty())
				continue;

			settings.add((SettingsElement) category);
			category.subSettings();

			for (RecordingDisplaySetting display : displays)
				((SettingsElement) category).getSubSettings().add(new RecordingSelectionSetting(display.recording));
		}
	}

	void setSelectedRecording(RecraftRecording selectedRecording) {
		recording = selectedRecording;
		setDisplayName(selectedRecording == null ? "§8[Nichts ausgewählt]" : selectedRecording.name.get());
//		icon(new IconData());
//		icon(selectedRecording == null ? Material.BARRIER : new IconData());

		if (container.mainSetting != null)
			updateName(selectedRecording);

		if (!(mc.currentScreen instanceof LabyModAddonsGui))
			return;

		ArrayList<SettingsElement> path = path();
		path.remove(path.size() - 1);
		if (selectedRecording != null)
			path.remove(path.size() - 1);

		mc.currentScreen.initGui();
	}

	public void updateName(RecraftRecording selectedRecording) {
		String name = container.mainSetting.getDisplayName();
		if (name.indexOf('\n') != -1)
			name = name.substring(0, name.indexOf('\n'));

		if (selectedRecording != null)
			name += "\n§8§o➡ " + selectedRecording.name.get();

		container.mainSetting.setDisplayName(name);
	}

	public RecraftRecording get() {
		return recording;
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
//		if (recording != null)
//			recording.mainSetting.drawIcon(x, y);
	}

	int toInt() {
		if (recording == null)
			return -1;

		AtomicInteger index = new AtomicInteger();
		iterate((i, rec) -> {
			if (rec == recording)
				index.set(i);
		});

		return index.get();
	}

	void fromInt(int index) {
		// All recordings must have been loaded before the selected one can be loaded
		TickScheduler.runAfterRenderTicks(() -> {
			iterate((i, rec) -> {
				if (i == index)
					setSelectedRecording(rec);
			});
		}, 1);
	}

	boolean execute(RecordingMode previousMode) {
		if (recording == null)
			return true;

		if (previousMode == RECIPE || recording.mode.get() == RECIPE) {
			TickScheduler.runAfterClientTicks(() -> recording.play(true), 1);
			return true;
		}

		if (recording.mode.get() == CRAFT)
			return !CraftPlayer.play(recording, recording::playSuccessor, false, false);

		return DecompressPlayer.play(recording);
	}

	@Override
	public ExtendedStorage<Object> getStorage() {
		return storage;
	}

	private class RecordingSelectionSetting extends ControlElement implements Laby3Setting<RecordingSelectionSetting, Object> {

		private final ExtendedStorage<Object> storage = new ExtendedStorage<>(e -> JsonNull.INSTANCE, e -> NULL, NULL);
		private final GuiButton button = new GuiButton(-2, 0, 0, 20, 20, "");
		private final RecraftRecording recording;

		private RecordingSelectionSetting(RecraftRecording recording) {
			super("§cNo name set", null);
			setSettingEnabled(false);
			this.recording = recording;
//			icon(recording == null ? Material.BARRIER : new IconData());
			setDisplayName(recording == null ? "§8Nichts auswählen" : recording.name.get());
		}

		@Override
		public int getObjectWidth() {
			return 0;
		}

		@Override
		public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
			super.mouseClicked(mouseX, mouseY, mouseButton);

			if (!button.mousePressed(mc, mouseX, mouseY))
				return;

			button.playPressSound(mc.getSoundHandler());
			setSelectedRecording(recording);
		}

		@Override
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
			super.draw(x, y, maxX, maxY, mouseX, mouseY);

			mouseOver = mouseX > x && mouseX < maxX && mouseY > y && mouseY < maxY;

			button.xPosition = maxX - 22 - 2;
			button.yPosition = y + 1;

			if (mouseOver) {
				boolean isButtonHovered = mouseX >= button.xPosition && mouseY >= button.yPosition && mouseX < button.xPosition + button.getButtonWidth() && mouseY < button.yPosition + 20;
				GlEngine.color(new Color(isButtonHovered ? 0x33FF33 : 0x3BB3FF));
			} else {
				GlStateManager.color(0.9f, 0.9f, 0.9f);
			}
			drawButtonIcon(button.xPosition, button.yPosition);

//			if (recording != null)
//				recording.mainSetting.drawIcon(x, y);
		}

		private void drawButtonIcon(int buttonX, int buttonY) {
			GlStateManager.enableBlend();
			mc.getTextureManager().bindTexture(ModTextures.MISC_MENU_POINT);

			mc.getTextureManager().bindTexture(new ResourceLocation("labymod/textures/misc/menu_point.png"));
			LabyMod.getInstance().getDrawUtils().drawTexture(buttonX + 3.5f, buttonY + 3.5f, 0, 0, 256, 256, 13, 13, 2);
		}

		@Override
		public ExtendedStorage<Object> getStorage() {
			return storage;
		}
	}

}