/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.chat.chat_menu;

import dev.l3g7.griefer_utils.laby4.settings.types.SwitchSettingImpl;
import dev.l3g7.griefer_utils.settings.BaseSetting;

public class EntryDisplaySetting extends SwitchSettingImpl { // TODO: impl EntryDisplaySetting

	public final ChatMenuEntry entry;

	public EntryDisplaySetting(ChatMenuEntry r, BaseSetting parent) {
		this.entry = r;
	}

	public void delete() {}

	/*
	private final SettingsElement parent;
	private boolean editHovered = false;
	public final ChatMenuEntry entry;

	public EntryDisplaySetting(ChatMenuEntry r, BaseSetting parent) {
		name("§f");
		this.parent = parent;
		this.entry = r;

		List<SettingsElement> entries = parent.getSubSettings().getElements();
		entries.add(entries.size() - 1, this);
		set(entry.enabled);
		callback(enabled -> entry.enabled = enabled);
		ChatMenu.saveEntries();
	}

	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (editHovered)
			Minecraft.getMinecraft().displayGuiScreen(new AddChatMenuEntryGui(this, Minecraft.getMinecraft().currentScreen));
	}

	public void delete() {
		parent.getSubSettings().getElements().remove(this);
		ChatReactor.saveEntries();
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		String displayName = getDisplayName();
		setDisplayName("§f");
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		setDisplayName(displayName);


		String trimmedName = drawUtils().trimStringToWidth(entry.name, maxX - x - 25 - 79);
		String trimmedCommand = drawUtils().trimStringToWidth(((String) entry.command), maxX - x - 25 - 79 - drawUtils().getStringWidth("➡ "));
		drawUtils().drawString(trimmedName + (trimmedName.equals(entry.name) ? "" : "…"), x + 25, y + 7 - 5);
		drawUtils().drawString("§o➡ " + trimmedCommand + (trimmedCommand.equals(entry.command) ? "" : "…"), x + 25, y + 7 + 5);
		entry.drawIcon(x + 3, y + 3, 16, 16);

		editHovered = mouseX > maxX - 70 && mouseX < maxX - 55 && mouseY > y + 4 && mouseY < y + 20;
		mc.getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/pencil.png"));
		drawUtils().drawTexture(maxX - 66 - (editHovered ? 4 : 3), y + (editHovered ? 3.5 : 4.5), 256, 256, editHovered ? 16 : 14, editHovered ? 16 : 14);
	}
*/
}