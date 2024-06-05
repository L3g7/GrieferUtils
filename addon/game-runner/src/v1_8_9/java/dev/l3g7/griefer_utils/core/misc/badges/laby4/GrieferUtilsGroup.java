/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.badges.laby4;

import dev.l3g7.griefer_utils.core.api.WebAPI;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.misc.badges.Badges;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.misc.server.GUClient;
import net.labymod.api.Laby;
import net.labymod.api.LabyAPI;
import net.labymod.api.Textures;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.client.component.format.TextDecoration;
import net.labymod.api.client.entity.Entity;
import net.labymod.api.client.entity.player.tag.tags.IconTag;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.Bounds;
import net.labymod.api.client.network.NetworkPlayerInfo;
import net.labymod.api.client.render.font.RenderableComponent;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.configuration.labymod.main.laby.multiplayer.TabListConfig;
import net.labymod.api.user.GameUserService;
import net.labymod.api.user.group.Group;
import net.labymod.api.user.group.GroupDisplayType;
import net.labymod.api.util.ColorUtil;
import net.labymod.core.client.gui.screen.activity.activities.ingame.playerlist.PlayerListRenderer;
import net.labymod.core.main.user.DefaultGameUser;
import net.labymod.core.main.user.badge.RankBadgeRenderer;
import net.labymod.core.main.user.group.tag.GroupIconTag;
import net.labymod.core.main.user.group.tag.GroupTextTag;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.awt.*;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static java.lang.Boolean.TRUE;
import static net.labymod.api.user.group.GroupDisplayType.ABOVE_HEAD;
import static net.labymod.api.user.group.GroupDisplayType.NONE;

@ExclusiveTo(LABY_4)
public class GrieferUtilsGroup extends Group {

	public static String icon = "icon";

	public final String title;
	private final int colorWithLabymod;
	private final int colorWithoutLabymod;
	public final TextColor titleColor;

	public GrieferUtilsGroup() {
		this(null, 0xFFFFFF, 0xFFFFFF);
	}

	public GrieferUtilsGroup(WebAPI.Data.SpecialBadge badge) {
		this(badge.title, badge.colorWithoutLabymod, badge.colorWithLabymod);
	}

	public GrieferUtilsGroup(String title, int colorWithoutLabymod, int colorWithLabymod) {
		super(999, "griefer_utils", "GrieferUtils", "", '\0', "", "", false);
		this.colorWithLabymod = colorWithLabymod;
		this.colorWithoutLabymod = colorWithoutLabymod;

		TextColor currentTitleColor = null;
		while (title != null && title.startsWith("§")) {
			int colorIndex = Integer.parseInt(String.valueOf(title.charAt(1)), 16);
			title = title.substring(2);
			currentTitleColor = ColorUtil.DEFAULT_COLORS[colorIndex];
		}

		this.titleColor = currentTitleColor;
		this.title = title;
	}

	@Override
	public boolean isDefault() {
		return true;
	}

	@Override
	public GroupDisplayType getDisplayType() {
		return title == null ? NONE : ABOVE_HEAD;
	}

	public void render(float x, float y) {
		render(Boolean.TRUE.equals(Laby.labyAPI().config().multiplayer().userIndicator().showUserIndicatorInPlayerList().get()), x, y);
	}

	private void render(boolean revealFamiliarUsers, float x, float y) {
		Color color = new Color(revealFamiliarUsers ? colorWithLabymod : colorWithoutLabymod);

		if (icon.equals("icon"))
			GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1);

		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		DrawUtils.bindTexture(new ResourceLocation("griefer_utils", "icons/icon.png"));
		DrawUtils.drawTexture(x, y, 255, 255, 8, 8, 1.1f);
		GlStateManager.color(1, 1, 1, 1);
	}

	public static void renderUserPercentage(int left, int width) {
		if (!Badges.showBadges() || !Badges.showPercentage.get())
			return;

		int x = width + left;

		int totalCount = mc().getNetHandler().getPlayerInfoMap().size();
		int familiarCount = 0;

		for (net.minecraft.client.network.NetworkPlayerInfo npi : mc().getNetHandler().getPlayerInfoMap())
			if (Laby.references().gameUserService().gameUser(npi.getGameProfile().getId()).isUsingLabyMod())
				familiarCount++;

		TabListConfig tlc = Laby.labyAPI().config().multiplayer().tabList();

		if (TRUE.equals(tlc.labyModBadge().get()) && TRUE.equals(tlc.labyModPercentage().get())) {
			int percent = totalCount == 0 ? 0 : (int) Math.round(familiarCount / (double) totalCount * 100);
			String labyModText = String.format("§7%d§8/§7%d §a%d%%", familiarCount, totalCount, percent);
			double delta = Laby.references().renderPipeline().textRenderer().width(labyModText) * 0.7 + 6.5;
			x -= delta;

			Textures.SpriteLabyMod.DEFAULT_WOLF_HIGH_RES.render(Stack.getDefaultEmptyStack(), x, 1.5f, 6.5f);
			x -= 3f;
		}


		familiarCount = 0;
		for (net.minecraft.client.network.NetworkPlayerInfo npi : mc().getNetHandler().getPlayerInfoMap())
			if (Laby.references().gameUserService().gameUser(npi.getGameProfile().getId()).visibleGroup() instanceof GrieferUtilsGroup)
				familiarCount++;

		int percent = totalCount == 0 ? 0 : (int) Math.round(familiarCount / (double) totalCount * 100);
		String text = GUClient.get().isAvailable() ? String.format("§7%d§8/§7%d §a%d%%", familiarCount, totalCount, percent) : "§c?";
		DrawUtils.drawRightString(text, x, 1.5, 0.7);

		DrawUtils.bindTexture(new ResourceLocation("griefer_utils", "icons/icon.png"));
		x -= Laby.references().renderPipeline().textRenderer().width(text) * 0.7;
		DrawUtils.drawTexture(x - 8, 1.25, 256, 256, 7, 7);
	}

	/**
	 * Ensure the ingame badge is displayed
	 */
	@Mixin(value = DefaultGameUser.class, remap = false)
	private static abstract class MixinDefaultGameUser {

		@Shadow
		public abstract @NotNull Group visibleGroup();

		@Inject(method = "isUsingLabyMod", at = @At("RETURN"), cancellable = true)
		private void injectIsUsingLabyMod(CallbackInfoReturnable<Boolean> cir) {
			if (!cir.getReturnValueZ() && visibleGroup() instanceof GrieferUtilsGroup)
				cir.setReturnValue(true);
		}

	}

	/**
	 * Title handling
	 */
	@Mixin(value = GroupTextTag.class, remap = false)
	private static class MixinGroupTextTag {

		@Shadow
		private @Nullable Group group;

		@Inject(method = "getRenderableComponent", at = @At("HEAD"), cancellable = true)
		private void injectGetRenderableComponent(CallbackInfoReturnable<RenderableComponent> cir) {
			if (!(group instanceof GrieferUtilsGroup guGroup))
				return;

			cir.setReturnValue(RenderableComponent.of(Component.text("GrieferUtils ")
				.color(TextColor.color(0xFFFFFF))
				.decorate(TextDecoration.BOLD)
				.append(Component.text(guGroup.title)
					.color(guGroup.titleColor))));
		}

	}

	/**
	 * Nametag badge rendering
	 */
	@Mixin(value = IconTag.class, remap = false)
	private static class MixinIconTag {

		@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	    private void injectrender(Stack stack, Entity entity, CallbackInfo ci) {
	    	if (!(((Object) this) instanceof GroupIconTag git))
				return;

			Group group = Reflection.get(git, "group");
			if (!(group instanceof GrieferUtilsGroup guGroup))
				return;

			if (guGroup.title == null)
				guGroup.render(0, 0);

			ci.cancel();
		}

	}

	/**
	 * Tablist badge rendering
	 */
	@Mixin(value = RankBadgeRenderer.class, remap = false)
	private static class MixinRankBadgeRenderer {

		@Shadow
		@Final
		private GameUserService gameUserService;

		@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	    private void injectRender(Stack stack, float x, float y, NetworkPlayerInfo player, CallbackInfo ci) {
		    Group group = gameUserService.gameUser(player.profile().getUniqueId()).visibleGroup();
			if (!(group instanceof GrieferUtilsGroup guGroup))
				return;

			guGroup.render(x, y);
			ci.cancel();
	    }

	}

	@Mixin(value = PlayerListRenderer.class, remap = false)
	private static class MixinPlayerListRenderer {

	    @Inject(method = "render", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
	    private void injectRender(Stack stack, LabyAPI labyAPI, Bounds bounds, boolean update, CallbackInfo ci, int screenWidth, float columnsWidth, float backgroundWidth, RenderableComponent headerRenderableComponent, RenderableComponent footerRenderableComponent, int x, int y) {
	    	renderUserPercentage(x, (int) backgroundWidth);
	    }

	}

}
