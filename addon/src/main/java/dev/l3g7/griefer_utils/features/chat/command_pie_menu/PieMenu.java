/*
 * This file is part of GrieferUtils https://github.com/L3g7/GrieferUtils.
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.features.chat.command_pie_menu;

import dev.l3g7.griefer_utils.core.event_bus.Disableable;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.events.MessageEvent;
import dev.l3g7.griefer_utils.event.events.MouseEvent;
import dev.l3g7.griefer_utils.event.events.render.RenderGameOverlayEvent;
import dev.l3g7.griefer_utils.event.events.render.ScaledResolutionInitEvent;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import dev.l3g7.griefer_utils.util.render.GlEngine;
import net.labymod.main.LabyMod;
import net.labymod.main.ModTextures;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.DrawUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;

public class PieMenu extends Gui implements Disableable {

	private final List<Pair<String, List<Pair<String, String>>>> allPages = new ArrayList<>();
	private Pair<String, List<Pair<String, String>>> currentPage;
	private int page = 0;

	private float lockedYaw = 0f;
	private float lockedPitch = 0f;
	private boolean prevCrosshairState;
	private long selectionStarted;
	private boolean open = false;
	private String hoveredCommand = null;

	public PieMenu() {
		EventRegisterer.register(this);
	}

	public void open(boolean animation, SettingsElement entryContainer) {
		if (open)
			return;

		open = true;
		selectionStarted = System.currentTimeMillis();
		if (!animation)
			selectionStarted -= 1572;

		lockedYaw = player().rotationYaw;
		lockedPitch = player().rotationPitch;
		prevCrosshairState = labyMod().getLabyModAPI().isCrosshairHidden();
		labyMod().getLabyModAPI().setCrosshairHidden(true);
		allPages.clear();

		for (SettingsElement pageElement : entryContainer.getSubSettings().getElements()) {
			if (!(pageElement instanceof PieMenuPageSetting))
				continue;

			List<Pair<String, String>> entries = new ArrayList<>();

			for (SettingsElement element : pageElement.getSubSettings().getElements()) {
				if (!(element instanceof PieMenuEntrySetting))
					continue;

				PieMenuEntrySetting entry = (PieMenuEntrySetting) element;
				String cb = entry.cityBuild.get().getDisplayName();
				String srv = MinecraftUtil.getServerFromScoreboard();
				if (cb.equals("Egal") || srv.equals(cb))
					entries.add(Pair.of(entry.name.get(), entry.command.get()));
			}

			allPages.add(Pair.of(((PieMenuPageSetting) pageElement).name.get(), entries));
		}

		if (allPages.size() != 0)
			switchToPage(0);
		else
			currentPage = Pair.of("", new ArrayList<>());
	}

	public void close() {
		if (!open)
			return;

		labyMod().getLabyModAPI().setCrosshairHidden(prevCrosshairState);
		open = false;
		if (player() == null)
			return;

		player().rotationYaw = lockedYaw;
		player().rotationPitch = lockedPitch;

		if (hoveredCommand == null)
			return;

		if (!MessageEvent.MessageSendEvent.post(hoveredCommand))
			player().sendChatMessage(hoveredCommand);
	}

	@EventListener
	public void render(RenderGameOverlayEvent event) {
		if (player() == null || player().getUniqueID() == null || player().hurtTime != 0) {
			close();
			return;
		}

		// Prevent mining / attacking
		Reflection.set(mc(), 2, "leftClickCounter");

		DrawUtils draw = LabyMod.getInstance().getDrawUtils();
		double radiusMouseBorder = draw.getHeight() / 12d;
		double midX = draw.getWidth() / 2d;
		double midY = draw.getHeight() / 2d;

		double lockedX = lockedYaw;
		double lockedY = lockedPitch;
		if (lockedY + radiusMouseBorder > 90)
			lockedY = 90 - (float) radiusMouseBorder;
		if (lockedY - radiusMouseBorder < -90)
			lockedY = -90 + (float) radiusMouseBorder;

		double radius = draw.getHeight() / 4d;
		double offsetX = lockedX - player().rotationYaw;
		double offsetY = lockedY - player().rotationPitch;
		double distance = Math.sqrt(offsetX * offsetX + offsetY * offsetY);
		double cursorX = midX - offsetX * 1.5;
		double cursorY = midY - offsetY * 1.5;
		draw.drawCenteredString(name(), midX += offsetX, (midY += offsetY) - radius / 2.5, radius / 70);


		long timePassed = System.currentTimeMillis() - selectionStarted;
		float animation = timePassed > 270f ? 1 : (float) Math.sin(timePassed / 200f);
		float skullScale = (20f + (float) radius / 2f) * animation;

		GlStateManager.pushMatrix();
		GlStateManager.translate(midX, midY, 0);
		GlStateManager.scale(skullScale, skullScale, skullScale);
		GlStateManager.rotate((float) (cursorY - midY), -1, 0, 0);
		GlStateManager.rotate((float) (cursorX - midX), 0, 1, 0);
		GlStateManager.disableLighting();
		draw.renderSkull(player().getGameProfile());
		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();

		hoveredCommand = null;
		for (int i = 0; i < currentPage.getValue().size(); i++)
			drawUnit(currentPage.getValue().get(i), midX, midY, radius, currentPage.getValue().size(), i, cursorX, cursorY, distance);

		GlStateManager.disableAlpha();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();

		if (offsetX == 0 && offsetY == 0) {
			cursorX = (int)cursorX;
			cursorY = (int)cursorY;
		}

		// draw crosshair
		draw.drawRect(cursorX, cursorY - 4, cursorX + 1, cursorY + 5, MAX_VALUE);
		draw.drawRect(cursorX - 4, cursorY, cursorX + 5, cursorY + 1, MAX_VALUE);

		draw.drawCenteredString(currentPage.getKey(), midX, midY + radius * 1.25, radius / 70);
		int currentPage = allPages.size() == 0 ? 0 : page + 1;
		draw.drawCenteredString(currentPage + "/" + allPages.size(), midX, midY + radius * 1.25 + radius / 7, radius / 70);
	}

	@SuppressWarnings("DuplicateExpressions")
	private void drawUnit(Pair<String, String> entry, double midX, double midY, double radius, int amount, int index, double cursorX, double cursorY, double distance) {
		long timePassed = System.currentTimeMillis() - (this.selectionStarted - 1072L);
		float animation = (float)(timePassed < 1572L ? Math.sin((float)timePassed / 1000f) : 1);
		double tau = Math.PI * 2;
		double destinationShift = 3.0707963267948966;
		double shift = destinationShift / (double) (animation * animation);

		double xNext = midX + radius * 2 * Math.cos((index + 1) * tau / amount + shift);
		double yNext = midY + radius * 2 * Math.sin((index + 1) * tau / amount + shift);
		double xAfterNext = midX + radius * 2 * Math.cos((index + 2) * tau / amount + shift);
		double yAfterNext = midY + radius * 2 * Math.sin((index + 2) * tau / amount + shift);

		double idk = (index + 1.5) * tau / amount;
		double x = midX + radius * Math.cos(idk + shift);
		double y = midY + radius * Math.sin(idk + shift);
		double finalDestX = midX + radius * Math.cos(idk + destinationShift);
		boolean insideOfTwo = cursorY > midY && index == 0 || cursorY < midY && index != 0;
		boolean inside = amount > 2 ? isInside(cursorX, cursorY, xAfterNext, yAfterNext, midX, midY, xNext, yNext) : amount == 1 || insideOfTwo;
		boolean hover = distance > 10 && inside && timePassed > 1500L;
		double size = radius / 70;
		if (size < 3)
			size = 1;

		double textureWidth = 9 * size;
		double textureHeight = 9 * size;

		Minecraft.getMinecraft().getTextureManager().bindTexture(ModTextures.MISC_MENU_POINT);

		GlEngine.color(new Color(hover ? 0x33FF33 : 0x3BB3FF));

		if (finalDestX - midX > -10 && finalDestX - midX < 10) {
			drawUtils().drawTexture(x - textureWidth / 2, y - 13, 256, 256, textureWidth, textureHeight);
			drawOptionTag(entry.getKey(), x, y, hover, radius, 0);
		} else if (finalDestX > midX) {
			drawUtils().drawTexture(x - 13 * size, y, 256, 256, textureWidth, textureHeight);
			drawOptionTag(entry.getKey(), x, y, hover, radius, 1);
		} else {
			drawUtils().drawTexture(x + 4, y, 256, 256, textureWidth, textureHeight);
			drawOptionTag(entry.getKey(), x, y, hover, radius, -1);
		}

		if (hover)
			hoveredCommand = entry.getValue();

		GL11.glLineWidth(2);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ModTextures.VOID);
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
	}

	private void drawOptionTag(String displayName, double x, double y, boolean hover, double radius, int alignment) {
		DrawUtils draw = LabyMod.getInstance().getDrawUtils();
		int stringWidth = draw.getStringWidth(displayName);
		int tagPadding = hover ? 3 : 2;
		double size = radius / 70;
		if (size < 3)
			size = 1;

		int tagHeight = (int) (9 * size);
		stringWidth = (int) (stringWidth * size);
		double tagX = x;
		double tagY = y + tagHeight;

		if (alignment < 0)
			tagX = x - stringWidth;
		else if (alignment == 0)
			tagX = x - stringWidth / 2d;

		draw.drawRect(tagX - tagPadding, tagY - tagHeight - tagPadding, tagX + stringWidth + tagPadding, tagY + tagPadding, 0x64141414);
		draw.drawRectBorder(tagX - tagPadding - 1, tagY - tagHeight - tagPadding - 1, tagX + stringWidth + tagPadding + 1, tagY + tagPadding + 1, hover ? MAX_VALUE : MIN_VALUE, 1);

		switch (alignment) {
			case -1: {
				draw.drawRightString(displayName, x, y, size);
				break;
			}
			case 0: {
				draw.drawCenteredString(displayName, x, y, size);
				break;
			}
			case 1: {
				draw.drawString(displayName, x, y, size);
			}
		}
	}

	private double sign(double px1, double py1, double px2, double py2, double px3, double py3) {
		return (px1 - px3) * (py2 - py3) - (px2 - px3) * (py1 - py3);
	}

	private boolean isInside(double pointX, double pointY, double px1, double py1, double px2, double py2, double px3, double py3) {
		boolean b1 = sign(pointX, pointY, px1, py1, px2, py2) < 0;
		boolean b2 = sign(pointX, pointY, px2, py2, px3, py3) < 0;
		boolean b3 = sign(pointX, pointY, px3, py3, px1, py1) < 0;
		return b1 == b2 && b2 == b3;
	}

	@EventListener
	public void lockMouseMovementInCircle(ScaledResolutionInitEvent event) {
		if (player() == null)
			return;

		double radius = (double)LabyMod.getInstance().getDrawUtils().getHeight() / 4 / 3;
		float centerX = this.lockedYaw;
		float centerY = this.lockedPitch;

		if (centerY + radius > 90)
			centerY = 90 - (float) radius;
		if (centerY - radius < -90)
			centerY = (float) (-90 + radius);

		double distanceY = centerY - player().rotationPitch;
		double distanceX = centerX - player().rotationYaw;
		double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

		if (!(distance > radius))
			return;

		double fromOriginToObjectX = player().rotationYaw - centerX;
		double fromOriginToObjectY = player().rotationPitch - centerY;
		double multiplier = radius / distance;
		player().rotationYaw = player().prevRotationYaw= (float) (centerX + fromOriginToObjectX * multiplier);
		player().rotationPitch = player().prevRotationPitch = (float) (centerY + fromOriginToObjectY * multiplier);
	}

	@EventListener
	public void onMouseInput(MouseEvent event) {
		if (event.dwheel != 0) {
			switchToPage(page + (event.dwheel > 0 ? -1 : 1));
			event.cancel();
			return;
		}

		if (!event.buttonstate)
			return;

		if (event.button > 1)
			return;

		switchToPage(page + (event.button == 0 ? -1 : 1));
	}

	private void switchToPage(int target) {
		if (allPages.size() == 0)
			return;

		page = MathHelper.clamp_int(target, 0, allPages.size() - 1);
		currentPage = allPages.get(page);
	}

	@Override
	public boolean isEnabled() {
		return open;
	}

}