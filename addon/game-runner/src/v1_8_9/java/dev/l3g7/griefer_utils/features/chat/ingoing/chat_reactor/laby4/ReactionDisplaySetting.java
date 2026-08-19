/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.ingoing.chat_reactor.laby4;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.util.ArrayUtil;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Icons;
import dev.l3g7.griefer_utils.labymod.laby4.settings.types.SwitchSettingImpl;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@ExclusiveTo(LABY_4) // NOTE LM3 ReactionDisplaySetting
public class ReactionDisplaySetting extends SwitchSettingImpl {

	public final ChatReaction reaction;

	public ReactionDisplaySetting(ChatReaction reaction) {
		this.reaction = reaction;
		initDisplay();
		callback(enabled -> {
			reaction.enabled = enabled;
			ChatReactor.saveEntries();
		});
	}

	@Override
	protected Widget[] createWidgets() {
		ButtonWidget widget = ButtonWidget.component(null, Icons.of("high_res/pencil_vec"), () ->
				mc().displayGuiScreen(new AddChatReactionGui(this, mc().currentScreen)))
			.addId("mods-setting-advanced-button"); // Fix for button size

		// ModsSettingWidget resets the widget and its icon, which causes it to disappear.
		// The component is not synced to the text property (bug?), so that stays.
		widget.icon().updateDefaultValue(widget.icon().get());

		return ArrayUtil.merge(Widget[]::new, super.createWidgets(), new Widget[]{
			widget
		});
	}

	public void initDisplay() {
		name(reaction.trigger, "§e[" + reaction.citybuild.getAbbreviation() + "] §r§o➡ " + reaction.command);
		icon(reaction.regEx ? "cpu" : "book_and_quill");
		set(reaction.enabled);
	}

	public void delete() {
		parent.unregister(kv -> kv.getValue() == this);
		ChatReactor.saveEntries();
	}

}