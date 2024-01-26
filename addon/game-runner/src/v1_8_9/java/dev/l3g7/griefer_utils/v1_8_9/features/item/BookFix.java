/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.item;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.MouseClickEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.v1_8_9.misc.ServerCheck;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.lwjgl.input.Mouse;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.*;
import static org.lwjgl.input.Keyboard.KEY_ESCAPE;

/**
 * Suppresses left-clicks on books and opens a preview when right-clicking.
 */
@Singleton
public class BookFix extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Bücher fixen")
		.description("Unterbindet Linksklick auf Bücher und öffnet eine Vorschau bei Rechtsklick.")
		.icon(Items.book);

	/**
	 * Fixes direct book interactions.
	 */
	@EventListener
	public void onMouse(MouseClickEvent.RightClickEvent event) {
		if (player() == null || mc().currentScreen != null)
			return;

		if (processClick(player().getHeldItem(), true))
			event.cancel();
	}

	/**
	 * Fixes book interactions in guis.
	 */
	@EventListener
	public void onMouseGui(GuiScreenEvent.MouseInputEvent.Pre event) {
		if (!Mouse.getEventButtonState() || !(mc().currentScreen instanceof GuiContainer))
			return;

		if (processClick(getStackUnderMouse(mc().currentScreen), Mouse.getEventButton() == 1))
			event.cancel();
	}

	/**
	 * @return {@code true} if the click should be canceled.
	 */
	private boolean processClick(ItemStack item, boolean openBook) {
		if (!ServerCheck.isOnGrieferGames())
			return false;

		if (item == null || (item.getItem() != Items.writable_book && item.getItem() != Items.written_book))
			return false;

		if (openBook) {
			// Fix missing tags
			NBTTagCompound tag = item.getTagCompound();
			if (tag == null)
				tag = new NBTTagCompound();

			if (!tag.hasKey("title"))
				tag.setString("title", "A book");
			if (!tag.hasKey("author"))
				tag.setString("author", "Me");
			if (!tag.hasKey("pages"))
				tag.setTag("pages", new NBTTagList());

			// Open preview
			mc().displayGuiScreen(new GuiBookPreview(player(), item));
		}

		return true;
	}

	/**
	 * A {@link GuiScreenBook} opening the last screen when closed.
	 */
	private static class GuiBookPreview extends GuiScreenBook {

		private final GuiScreen previousScreen = Minecraft.getMinecraft().currentScreen;

		private GuiBookPreview(EntityPlayer player, ItemStack book) {
			super(player, book, false);
		}

		public void keyTyped(char typedChar, int keyCode) {
			if (keyCode == KEY_ESCAPE)
				Minecraft.getMinecraft().displayGuiScreen(previousScreen);
		}

		public void actionPerformed(GuiButton button) {
			if (button.enabled && button.id == 0)
				Minecraft.getMinecraft().displayGuiScreen(previousScreen);
			else
				super.actionPerformed(button);
		}

	}

}
