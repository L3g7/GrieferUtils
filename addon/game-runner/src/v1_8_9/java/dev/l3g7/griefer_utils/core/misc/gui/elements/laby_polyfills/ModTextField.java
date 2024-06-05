/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;

import java.awt.*;

/**
 * Based on LabyMod 3's ModTextField.
 * NOTE: remove?
 */
public class ModTextField extends Gui {
    private final int id;
    private final FontRenderer fontRendererInstance;
    public int xPosition;
    public int yPosition;
    public int width;
    public int height;
    private String text = "";
    private int maxStringLength = 32;
    private int cursorCounter;
    private boolean enableBackgroundDrawing = true;
    private boolean canLoseFocus = true;
    private boolean isFocused;
    private boolean isEnabled = true;
    private int lineScrollOffset;
    private int cursorPosition;
    private int selectionEnd;
    private int enabledColor = 14737632;
    private int disabledColor = 7368816;
    private boolean visible = true;
    private Predicate<String> field_175209_y = Predicates.alwaysTrue();
    private boolean blackBox = true;
    private boolean modPasswordBox = false;
    private String modBlacklistWord = "";
    private boolean colorBarEnabled = false;
    private EnumChatFormatting hoveredModColor = null;
    private String colorAtCursor = null;
    private String placeHolder;
    private boolean backgroundColor = false;

    public ModTextField(int componentId, FontRenderer fontrenderer, int x, int y, int par5Width, int par6Height) {
        this.id = componentId;
        this.fontRendererInstance = fontrenderer;
        this.xPosition = x;
        this.yPosition = y;
        this.width = par5Width;
        this.height = par6Height;
    }

    public void setBackgroundColor(boolean backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public boolean isBackgroundColor() {
        return this.backgroundColor;
    }

    public void updateCursorCounter() {
        ++this.cursorCounter;
    }

    public void setText(String p_146180_1_) {
        if (this.field_175209_y.apply(p_146180_1_)) {
            if (p_146180_1_.length() > this.maxStringLength) {
                this.text = p_146180_1_.substring(0, this.maxStringLength);
            } else {
                this.text = p_146180_1_;
            }

            this.setCursorPosition(0);
        }

    }

    public void setPlaceHolder(String placeHolder) {
        this.placeHolder = placeHolder;
    }

    public String getPlaceHolder() {
        return this.placeHolder;
    }

    public String getText() {
        return this.text;
    }

    public String getSelectedText() {
        int i = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int j = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        return this.text.substring(i, j);
    }

    public void func_175205_a(Predicate<String> p_175205_1_) {
        this.field_175209_y = p_175205_1_;
    }

    public void writeText(String p_146191_1_) {
        String s = "";
        String s1 = ChatAllowedCharacters.filterAllowedCharacters(p_146191_1_);
        int i = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int j = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        int k = this.maxStringLength - this.text.length() - (i - j);
        if (this.text.length() > 0) {
            s = s + this.text.substring(0, i);
        }

        int l;
        if (k < s1.length()) {
            s = s + s1.substring(0, k);
            l = k;
        } else {
            s = s + s1;
            l = s1.length();
        }

        if (this.text.length() > 0 && j < this.text.length()) {
            s = s + this.text.substring(j);
        }

        if (this.field_175209_y.apply(s)) {
            this.text = s;
            this.moveCursorBy(i - this.selectionEnd + l);
        }

    }

    public void deleteWords(int p_146177_1_) {
        if (this.text.length() != 0) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                this.deleteFromCursor(this.getNthWordFromCursor(p_146177_1_) - this.cursorPosition);
            }
        }

    }

    public void deleteFromCursor(int p_146175_1_) {
        if (this.text.length() != 0) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                boolean flag = p_146175_1_ < 0;
                int i = flag ? this.cursorPosition + p_146175_1_ : this.cursorPosition;
                int j = flag ? this.cursorPosition : this.cursorPosition + p_146175_1_;
                String s = "";
                if (i >= 0) {
                    s = this.text.substring(0, i);
                }

                if (j < this.text.length()) {
                    s = s + this.text.substring(j);
                }

                if (this.field_175209_y.apply(s)) {
                    this.text = s;
                    if (flag) {
                        this.moveCursorBy(p_146175_1_);
                    }
                }
            }
        }

    }

    public int getId() {
        return this.id;
    }

    public int getNthWordFromCursor(int p_146187_1_) {
        return this.getNthWordFromPos(p_146187_1_, this.getCursorPosition());
    }

    public int getNthWordFromPos(int p_146183_1_, int p_146183_2_) {
        return this.func_146197_a(p_146183_1_, p_146183_2_, true);
    }

    public int func_146197_a(int p_146197_1_, int p_146197_2_, boolean p_146197_3_) {
        int i = p_146197_2_;
        boolean flag = p_146197_1_ < 0;
        int j = Math.abs(p_146197_1_);

        for(int k = 0; k < j; ++k) {
            if (!flag) {
                int l = this.text.length();
                i = this.text.indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while(p_146197_3_ && i < l && this.text.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while(p_146197_3_ && i > 0 && this.text.charAt(i - 1) == ' ') {
                    --i;
                }

                while(i > 0 && this.text.charAt(i - 1) != ' ') {
                    --i;
                }
            }
        }

        return i;
    }

    public void moveCursorBy(int p_146182_1_) {
        this.setCursorPosition(this.selectionEnd + p_146182_1_);
    }

    public void setCursorPosition(int p_146190_1_) {
        this.cursorPosition = p_146190_1_;
        int i = this.text.length();
        this.cursorPosition = MathHelper.clamp_int(this.cursorPosition, 0, i);
        this.setSelectionPos(this.cursorPosition);
    }

    public void setCursorPositionZero() {
        this.setCursorPosition(0);
    }

    public void setCursorPositionEnd() {
        this.setCursorPosition(this.text.length());
    }

    public boolean textboxKeyTyped(char p_146201_1_, int p_146201_2_) {
        if (!this.isFocused) {
            return false;
        } else if (GuiScreen.isKeyComboCtrlA(p_146201_2_)) {
            this.setCursorPositionEnd();
            this.setSelectionPos(0);
            return true;
        } else if (GuiScreen.isKeyComboCtrlC(p_146201_2_)) {
            if (!this.isPasswordBox()) {
                GuiScreen.setClipboardString(this.getSelectedText());
            }

            return true;
        } else if (GuiScreen.isKeyComboCtrlV(p_146201_2_)) {
            if (this.isEnabled) {
                this.writeText(GuiScreen.getClipboardString());
            }

            return true;
        } else if (GuiScreen.isKeyComboCtrlX(p_146201_2_)) {
            if (!this.isPasswordBox()) {
                GuiScreen.setClipboardString(this.getSelectedText());
            }

            if (this.isEnabled) {
                this.writeText("");
            }

            return true;
        } else {
            switch (p_146201_2_) {
                case 14:
                    if (GuiScreen.isCtrlKeyDown()) {
                        if (this.isEnabled) {
                            this.deleteWords(-1);
                        }
                    } else if (this.isEnabled) {
                        this.deleteFromCursor(-1);
                    }

                    return true;
                case 199:
                    if (GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(0);
                    } else {
                        this.setCursorPositionZero();
                    }

                    return true;
                case 203:
                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(-1, this.getSelectionEnd()));
                        } else {
                            this.setSelectionPos(this.getSelectionEnd() - 1);
                        }
                    } else if (GuiScreen.isCtrlKeyDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(-1));
                    } else {
                        this.moveCursorBy(-1);
                    }

                    return true;
                case 205:
                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(1, this.getSelectionEnd()));
                        } else {
                            this.setSelectionPos(this.getSelectionEnd() + 1);
                        }
                    } else if (GuiScreen.isCtrlKeyDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(1));
                    } else {
                        this.moveCursorBy(1);
                    }

                    return true;
                case 207:
                    if (GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(this.text.length());
                    } else {
                        this.setCursorPositionEnd();
                    }

                    return true;
                case 211:
                    if (GuiScreen.isCtrlKeyDown()) {
                        if (this.isEnabled) {
                            this.deleteWords(1);
                        }
                    } else if (this.isEnabled) {
                        this.deleteFromCursor(1);
                    }

                    return true;
                default:
                    if (ChatAllowedCharacters.isAllowedCharacter(p_146201_1_)) {
                        if (this.isEnabled) {
                            this.writeText(Character.toString(p_146201_1_));
                        }

                        return true;
                    } else {
                        return false;
                    }
            }
        }
    }

    public boolean mouseClicked(int p_146192_1_, int p_146192_2_, int p_146192_3_) {
        boolean flag = p_146192_1_ >= this.xPosition && p_146192_1_ < this.xPosition + this.width && p_146192_2_ >= this.yPosition && p_146192_2_ < this.yPosition + this.height;
        if (this.colorBarEnabled && this.hoveredModColor != null) {
            this.writeText("&" + this.hoveredModColor.toString().substring(1));
            return true;
        } else {
            if (this.canLoseFocus) {
                this.setFocused(flag);
            }

            if (this.isFocused && flag && p_146192_3_ == 0) {
                int i = p_146192_1_ - this.xPosition;
                if (this.enableBackgroundDrawing) {
                    i -= 4;
                }

                String s = this.fontRendererInstance.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());
                this.setCursorPosition(this.fontRendererInstance.trimStringToWidth(s, i).length() + this.lineScrollOffset);
            }

            return this.isFocused;
        }
    }

    public void drawTextBox() {
        if (this.getVisible()) {
            if (!this.getBlacklistWord().isEmpty() && this.getText().contains(this.getBlacklistWord())) {
                this.setText(this.getText().replace(this.getBlacklistWord(), ""));
            }

            if (this.getEnableBackgroundDrawing()) {
                if (this.blackBox) {
                    drawRect(this.xPosition - 1, this.yPosition - 1, this.xPosition + this.width + 1, this.yPosition + this.height + 1, -6250336);
                    drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, -16777216);
                } else if (this.isFocused) {
	                DrawUtils.drawRectBorder((double)(this.xPosition - 1), (double)(this.yPosition - 1), (double)(this.xPosition + this.width + 1), (double)(this.yPosition + this.height + 1), toRGB(220, 220, 225, 62), 1.0);
	                DrawUtils.drawRectangle(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, toRGB(0, 0, 3, 180));
                } else {
                    drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, toRGB(70, 60, 53, 122));
                    drawRect(this.xPosition + 1, this.yPosition + 1, this.xPosition + this.width - 1, this.yPosition + this.height - 1, toRGB(0, 0, 3, 180));
                }
            }

            int i = this.isEnabled ? this.enabledColor : this.disabledColor;
            int j = this.cursorPosition - this.lineScrollOffset;
            int k = this.selectionEnd - this.lineScrollOffset;
            String theText = this.getText().substring(this.lineScrollOffset);
            if (this.isPasswordBox()) {
                theText = theText.replaceAll(".", "*");
            }

            String s = this.fontRendererInstance.trimStringToWidth(theText, this.getWidth());
            boolean flag = j >= 0 && j <= s.length();
            boolean flag1 = this.isFocused && this.cursorCounter / 6 % 2 == 0 && flag;
            int l = this.enableBackgroundDrawing ? this.xPosition + 4 : this.xPosition;
            int i1 = this.enableBackgroundDrawing ? this.yPosition + (this.height - 8) / 2 : this.yPosition;
            int j1 = l;
            this.colorAtCursor = null;
            if (k > s.length()) {
                k = s.length();
            }

            if (s.length() > 0) {
                String s1 = flag ? s.substring(0, j) : s;
                j1 = l + this.fontRendererInstance.getStringWidth(this.visualColorForText(s1, true));
            }

            boolean placeHolder = this.placeHolder != null && this.getText().isEmpty() && !this.isFocused;
            boolean flag2 = this.cursorPosition < this.text.length() || this.text.length() >= this.getMaxStringLength();
            int k1 = j1;
            if (!flag) {
                k1 = j > 0 ? l + this.width : l;
            } else if (flag2) {
                k1 = j1 - 1;
                --j1;
            }

            if (s.length() > 0 && flag && j < s.length()) {
                int var10000 = j1 + this.fontRendererInstance.getStringWidth(this.visualColorForText(s.substring(j), false));
            }

            this.fontRendererInstance.drawStringWithShadow(this.visualColorForText(s, false), (float)l, (float)i1, i);
            if (flag1 && !placeHolder) {
                if (flag2) {
                    Gui.drawRect(k1, i1 - 1, k1 + 1, i1 + 1 + this.fontRendererInstance.FONT_HEIGHT, -3092272);
                } else {
                    this.fontRendererInstance.drawStringWithShadow("_", (float)k1, (float)i1, i);
                }
            }

            if (k != j) {
                int l1 = l + this.fontRendererInstance.getStringWidth(s.substring(0, k));
                this.drawCursorVertical(k1, i1 - 1, l1 - 1, i1 + 1 + this.fontRendererInstance.FONT_HEIGHT);
            }

            if (placeHolder) {
                this.drawString(this.fontRendererInstance, this.placeHolder, k1, i1, Color.LIGHT_GRAY.getRGB());
            }
        }

    }

    public void drawColorBar(int mouseX, int mouseY) {
        if (this.colorBarEnabled) {
            this.hoveredModColor = null;
            int ll = 9;
            int pX = this.xPosition + this.width / 2 - (EnumChatFormatting.values().length * ll - ll) / 2;
            int pY = this.yPosition + this.height + 5;
	        EnumChatFormatting[] var6 = EnumChatFormatting.values();
            int var7 = var6.length;

            for(int var8 = 0; var8 < var7; ++var8) {
	            EnumChatFormatting color = var6[var8];
				String colorChar = color.toString().substring(1);
                boolean hovered = mouseX > pX - ll / 2 && mouseX < pX + ll / 2 && mouseY > pY - 1 && mouseY < pY + 9;
                if (hovered) {
                    this.hoveredModColor = color;
                }

                if (this.colorAtCursor != null && this.colorAtCursor.equals(colorChar)) {
                    hovered = true;
                }

                drawRect(pX - ll / 2, pY - 1, pX + ll / 2, pY + 9, !hovered ? toRGB(120, 120, 120, 120) : Integer.MAX_VALUE);
                DrawUtils.drawCenteredString(color.toString() + colorChar, (double)pX, (double)pY);
                pX += ll;
            }
        }

    }

    private String visualColorForText(String text, boolean saveCursorColor) {
        String coloredString = "";
        boolean foundColor = false;

        for(int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (c == '&' && i != text.length() - 1) {
                if (foundColor) {
                    coloredString = coloredString + "&";
                }

                foundColor = true;
            } else {
                if (foundColor) {
                    foundColor = false;
                    coloredString = coloredString + "§" + c + '&';
                    if (saveCursorColor) {
                        this.colorAtCursor = "" + c;
                    }
                }

                coloredString = coloredString + c;
            }
        }

        return coloredString;
    }

    private void drawCursorVertical(int p_146188_1_, int p_146188_2_, int p_146188_3_, int p_146188_4_) {
        int j;
        if (p_146188_1_ < p_146188_3_) {
            j = p_146188_1_;
            p_146188_1_ = p_146188_3_;
            p_146188_3_ = j;
        }

        if (p_146188_2_ < p_146188_4_) {
            j = p_146188_2_;
            p_146188_2_ = p_146188_4_;
            p_146188_4_ = j;
        }

        if (p_146188_3_ > this.xPosition + this.width) {
            p_146188_3_ = this.xPosition + this.width;
        }

        if (p_146188_1_ > this.xPosition + this.width) {
            p_146188_1_ = this.xPosition + this.width;
        }

        Tessellator tessellator = Tessellator.getInstance();
	    WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();
        GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(5387);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos((double)p_146188_1_, (double)p_146188_4_, 0.0).endVertex();
        worldrenderer.pos((double)p_146188_3_, (double)p_146188_4_, 0.0).endVertex();
        worldrenderer.pos((double)p_146188_3_, (double)p_146188_2_, 0.0).endVertex();
        worldrenderer.pos((double)p_146188_1_, (double)p_146188_2_, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }

    public void setMaxStringLength(int p_146203_1_) {
        this.maxStringLength = p_146203_1_;
        if (this.text.length() > p_146203_1_) {
            this.text = this.text.substring(0, p_146203_1_);
        }

    }

    public int getMaxStringLength() {
        return this.maxStringLength;
    }

    public int getCursorPosition() {
        return this.cursorPosition;
    }

    public void setBlacklistWord(String modBlacklistWords) {
        this.modBlacklistWord = modBlacklistWords;
    }

    public String getBlacklistWord() {
        return this.modBlacklistWord;
    }

    public void setPasswordBox(boolean modPasswordBox) {
        this.modPasswordBox = modPasswordBox;
    }

    public boolean isPasswordBox() {
        return this.modPasswordBox;
    }

    public boolean getEnableBackgroundDrawing() {
        return this.enableBackgroundDrawing;
    }

    public void setEnableBackgroundDrawing(boolean p_146185_1_) {
        this.enableBackgroundDrawing = p_146185_1_;
    }

    public boolean isBlackBox() {
        return this.blackBox;
    }

    public void setBlackBox(boolean blackBox) {
        this.blackBox = blackBox;
    }

    public void setTextColor(int p_146193_1_) {
        this.enabledColor = p_146193_1_;
    }

    public void setDisabledTextColour(int p_146204_1_) {
        this.disabledColor = p_146204_1_;
    }

    public void setFocused(boolean p_146195_1_) {
        if (p_146195_1_ && !this.isFocused) {
            this.cursorCounter = 0;
        }

        this.isFocused = p_146195_1_;
    }

    public boolean isFocused() {
        return this.isFocused;
    }

    public void setEnabled(boolean p_146184_1_) {
        this.isEnabled = p_146184_1_;
    }

    public int getSelectionEnd() {
        return this.selectionEnd;
    }

    public int getWidth() {
        return this.getEnableBackgroundDrawing() ? this.width - 8 : this.width;
    }

    public void setSelectionPos(int p_146199_1_) {
        int i = this.text.length();
        if (p_146199_1_ > i) {
            p_146199_1_ = i;
        }

        if (p_146199_1_ < 0) {
            p_146199_1_ = 0;
        }

        this.selectionEnd = p_146199_1_;
        if (this.fontRendererInstance != null) {
            if (this.lineScrollOffset > i) {
                this.lineScrollOffset = i;
            }

            int j = this.getWidth();
            String s = this.fontRendererInstance.trimStringToWidth(this.text.substring(this.lineScrollOffset), j);
            int k = s.length() + this.lineScrollOffset;
            if (p_146199_1_ == this.lineScrollOffset) {
                this.lineScrollOffset -= this.fontRendererInstance.trimStringToWidth(this.text, j, true).length();
            }

            if (p_146199_1_ > k) {
                this.lineScrollOffset += p_146199_1_ - k;
            } else if (p_146199_1_ <= this.lineScrollOffset) {
                this.lineScrollOffset -= this.lineScrollOffset - p_146199_1_;
            }

            this.lineScrollOffset = MathHelper.clamp_int(this.lineScrollOffset, 0, i);
        }

    }

    public void setCanLoseFocus(boolean p_146205_1_) {
        this.canLoseFocus = p_146205_1_;
    }

    public boolean getVisible() {
        return this.visible;
    }

    public void setVisible(boolean p_146189_1_) {
        this.visible = p_146189_1_;
    }

    public boolean isColorBarEnabled() {
        return this.colorBarEnabled;
    }

    public void setColorBarEnabled(boolean colorBar) {
        this.colorBarEnabled = colorBar;
    }

	public static int toRGB(int r, int g, int b, int a) {
		return (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255) << 0;
	}
}
