/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.chat_filter.chat_filter_webhooks.impl;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.settings.types.StringSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.chat.chat_filter.chat_filter_webhooks.ChatFilterWebhooks;
import net.labymod.api.client.chat.filter.ChatFilter;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.annotation.SpriteTexture;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.event.client.chat.advanced.AdvancedChatTabMessageEvent;
import net.labymod.core.client.chat.filter.DefaultFilterChatService;
import net.minecraft.util.IChatComponent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.features.chat.chat_filter.chat_filter_webhooks.ChatFilterWebhooks.webhooks;

@ExclusiveTo(LABY_4)
public class FilterWebhooksLaby4 {

	public static void hookToSettings(ChatFilter filter, @Nullable Setting parent, CallbackInfoReturnable<List<Setting>> cir) {
		if (!ChatFilterWebhooks.enabled.get())
			return;

		List<Setting> settings = cir.getReturnValue();
		String url = webhooks.get(filter.id().toString());

		// Create settings
		StringSetting urlInput = StringSetting.create()
			.name("Webhook-URL")
			.placeholder("https://discord.com/api/webhooks/...")
			.validator(v -> ChatFilterWebhooks.HOOK_URL_PATTERN.matcher(v).matches())
			.set(url == null ? "" : url)
			.enabled(url != null)
			.extend()
			.callback(v -> {
				webhooks.put(filter.id().toString(), v);
				ChatFilterWebhooks.saveWebhooks();
			});

		SwitchSetting shouldSend = SwitchSetting.create()
			.name("An Discord-Webhook senden")
			.set(url != null)
			.callback(v -> {
				urlInput.enabled(v);
				webhooks.put(filter.id().toString(), v && !urlInput.get().isEmpty() ? urlInput.get() : null);
				ChatFilterWebhooks.saveWebhooks();
			});

		// Bind settings
		shouldSend.create(parent);
		settings.add((Setting) shouldSend);

		urlInput.create(parent);
		settings.add((Setting) urlInput);
	}

	public static void hookApplyChatFilter(ChatFilter filter, IChatComponent component) {
		Integer color = filter.shouldChangeBackground().get() ? filter.backgroundColor().get() : null;
		ChatFilterWebhooks.triggerWebhook(webhooks.get(filter.id().toString()), component, filter.name().get(), color);
	}

	@ExclusiveTo(LABY_4)
	@Mixin(Config.class)
	public static class ConfigMixin {

		@Inject(method = "toSettings(Lnet/labymod/api/configuration/settings/Setting;Lnet/labymod/api/configuration/loader/annotation/SpriteTexture;)Ljava/util/List;", at = @At("RETURN"), remap = false)
		public void onToSettings(@Nullable Setting parent, SpriteTexture texture, CallbackInfoReturnable<List<Setting>> cir) {
			if (!((Config) c(this) instanceof ChatFilter filter))
				return;

			hookToSettings(filter, parent, cir);
		}

	}

	@ExclusiveTo(LABY_4)
	@Mixin(value = DefaultFilterChatService.class, remap = false)
	public static class DefaultFilterChatServiceMixin {

		@Final
		@Shadow
		private List<ChatFilter> matchingChatFilters;

		@Inject(method = "applyChatFilter", at = @At("RETURN"), remap = false)
		public void onApplyChatFilter(AdvancedChatTabMessageEvent event, CallbackInfo ci) {
			if (event.isCancelled())
				return;

			for (ChatFilter filter : matchingChatFilters)
				hookApplyChatFilter(filter, (IChatComponent) event.component());
		}

	}

}
