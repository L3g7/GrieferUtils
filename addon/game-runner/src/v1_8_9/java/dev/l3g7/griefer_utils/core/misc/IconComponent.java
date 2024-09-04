/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.Style;
import net.labymod.api.client.gui.icon.Icon;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

/**
 * A custom ChatComponent for LabyMod 3 imitating LabyMod 4's IconComponent.
 */
public abstract class IconComponent extends ChatComponentStyle {

	private static final char ICON_MARKER = '图';
	// KV lookup for creating new icons
	private static final HashMap<String, IconComponent> iconMap = new HashMap<>();
	// Index-based lookup for rendering
	private static final ArrayList<IconComponent> icons = new ArrayList<>();

	protected final int size;
	protected String id;

	public static ChatComponentStyle head(String name) {
		if (LABY_4.isActive())
			return (ChatComponentStyle) Component.icon(Icon.head(name), Style.empty(), 8);

		return iconMap.computeIfAbsent("head/" + name, v -> new HeadIconComponent(name).generateId());
	}

	private IconComponent(int size) {
		this.size = size;
	}

	@Override
	public String getUnformattedTextForChat() {
		return id;
	}

	@Override
	public IChatComponent createCopy() {
		IconComponent copy = copy();
		copy.setChatStyle(getChatStyle().createShallowCopy());
		copy.id = id;

		for (IChatComponent sibling : this.getSiblings())
			copy.appendSibling(sibling.createCopy());

		return copy;
	}

	protected IconComponent generateId() {
		int idx;
		synchronized (icons) {
			idx = icons.size();
			icons.add(this);
		}

		id = "§" + ICON_MARKER + idx + "\0";
		return this;
	}

	protected abstract IconComponent copy();

	protected abstract void draw(float x, float y, float alpha);

	private static class HeadIconComponent extends IconComponent {

		private final String name;

		public HeadIconComponent(String name) {
			super(8);
			this.name = name;
		}

		@Override
		public IconComponent copy() {
			return new HeadIconComponent(name);
		}

		@Override
		protected void draw(float x, float y, float alpha) {
			NetworkPlayerInfo playerInfo = mc().getNetHandler().getPlayerInfo(name);
			if (playerInfo == null)
				return;

			DrawUtils.bindTexture(playerInfo.getLocationSkin());
			DrawUtils.drawTexture(x, y, 32, 32, 32, 32, 8, 8, 2);
			DrawUtils.drawTexture(x, y, 160, 32, 32, 32, 8, 8, 2);
		}

	}

	@ExclusiveTo(LABY_3)
	@Mixin(FontRenderer.class)
	public static abstract class MixinFontRenderer {

		@Shadow
		public int FONT_HEIGHT = 9;
		@Shadow
		public Random fontRandom = new Random();
		@Shadow
		private int[] colorCode = new int[32];
		@Shadow
		private float posX, posY, red, blue, green, alpha;
		@Shadow
		private int textColor;
		@Shadow
		private boolean unicodeFlag, randomStyle, boldStyle, italicStyle, underlineStyle, strikethroughStyle;

		@Shadow
		public abstract int getCharWidth(char character);

		@Shadow
		protected abstract float renderChar(char ch, boolean italic);

		/**
		 * Mostly the original method, except if noted otherwise
		 */
		@Inject(method = "renderStringAtPos", at = @At(value = "HEAD"), cancellable = true)
		private void renderStringAtPos(String text, boolean shadow, CallbackInfo ci) {
			ci.cancel();

			for (int i = 0; i < text.length(); ++i) {
				char c0 = text.charAt(i);
				int i1;
				int j1;
				if (c0 == 167 && i + 1 < text.length()) {
					// Changes begin here
					if (text.charAt(i + 1) == ICON_MARKER) {
						int endIdx = text.indexOf('\0', i + 1);
						IconComponent icon = icons.get(Integer.parseInt(text.substring(i + 2, endIdx)));

						if (!shadow) { // Skip shadow layer
							icon.draw(posX, posY - 1, alpha);
							GlStateManager.enableAlpha();
						}

						posX += icon.size;
						i = endIdx;
						continue;
					}
					// Changes end here
					i1 = "0123456789abcdefklmnor".indexOf(text.toLowerCase(Locale.ENGLISH).charAt(i + 1));
					if (i1 < 16) {
						this.randomStyle = false;
						this.boldStyle = false;
						this.strikethroughStyle = false;
						this.underlineStyle = false;
						this.italicStyle = false;
						if (i1 < 0) {
							i1 = 15;
						}

						if (shadow) {
							i1 += 16;
						}

						j1 = this.colorCode[i1];
						this.textColor = j1;
						GlStateManager.color((float) (j1 >> 16) / 255.0F, (float) (j1 >> 8 & 255) / 255.0F, (float) (j1 & 255) / 255.0F, this.alpha);
					} else if (i1 == 16) {
						this.randomStyle = true;
					} else if (i1 == 17) {
						this.boldStyle = true;
					} else if (i1 == 18) {
						this.strikethroughStyle = true;
					} else if (i1 == 19) {
						this.underlineStyle = true;
					} else if (i1 == 20) {
						this.italicStyle = true;
					} else {
						this.randomStyle = false;
						this.boldStyle = false;
						this.strikethroughStyle = false;
						this.underlineStyle = false;
						this.italicStyle = false;
						GlStateManager.color(this.red, this.blue, this.green, this.alpha);
					}

					++i;
				} else {
					i1 = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(c0);
					if (this.randomStyle && i1 != -1) {
						j1 = this.getCharWidth(c0);

						char c1;
						do {
							i1 = this.fontRandom.nextInt("ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".length());
							c1 = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".charAt(i1);
						} while (j1 != this.getCharWidth(c1));

						c0 = c1;
					}

					float f1 = i1 != -1 && !this.unicodeFlag ? 1.0F : 0.5F;
					boolean flag = (c0 == 0 || i1 == -1 || this.unicodeFlag) && shadow;
					if (flag) {
						this.posX -= f1;
						this.posY -= f1;
					}

					float f = this.renderChar(c0, this.italicStyle);
					if (flag) {
						this.posX += f1;
						this.posY += f1;
					}

					if (this.boldStyle) {
						this.posX += f1;
						if (flag) {
							this.posX -= f1;
							this.posY -= f1;
						}

						this.renderChar(c0, this.italicStyle);
						this.posX -= f1;
						if (flag) {
							this.posX += f1;
							this.posY += f1;
						}

						++f;
					}

					Tessellator tessellator1;
					WorldRenderer worldrenderer1;
					if (this.strikethroughStyle) {
						tessellator1 = Tessellator.getInstance();
						worldrenderer1 = tessellator1.getWorldRenderer();
						GlStateManager.disableTexture2D();
						worldrenderer1.begin(7, DefaultVertexFormats.POSITION);
						worldrenderer1.pos(this.posX, this.posY + (float) (this.FONT_HEIGHT / 2), 0.0).endVertex();
						worldrenderer1.pos(this.posX + f, this.posY + (float) (this.FONT_HEIGHT / 2), 0.0).endVertex();
						worldrenderer1.pos(this.posX + f, this.posY + (float) (this.FONT_HEIGHT / 2) - 1.0F, 0.0).endVertex();
						worldrenderer1.pos(this.posX, this.posY + (float) (this.FONT_HEIGHT / 2) - 1.0F, 0.0).endVertex();
						tessellator1.draw();
						GlStateManager.enableTexture2D();
					}

					if (this.underlineStyle) {
						tessellator1 = Tessellator.getInstance();
						worldrenderer1 = tessellator1.getWorldRenderer();
						GlStateManager.disableTexture2D();
						worldrenderer1.begin(7, DefaultVertexFormats.POSITION);
						int l = this.underlineStyle ? -1 : 0;
						worldrenderer1.pos(this.posX + (float) l, this.posY + (float) this.FONT_HEIGHT, 0.0).endVertex();
						worldrenderer1.pos(this.posX + f, this.posY + (float) this.FONT_HEIGHT, 0.0).endVertex();
						worldrenderer1.pos(this.posX + f, this.posY + (float) this.FONT_HEIGHT - 1.0F, 0.0).endVertex();
						worldrenderer1.pos(this.posX + (float) l, this.posY + (float) this.FONT_HEIGHT - 1.0F, 0.0).endVertex();
						tessellator1.draw();
						GlStateManager.enableTexture2D();
					}

					this.posX += (float) ((int) f);
				}
			}
		}

	}

}
