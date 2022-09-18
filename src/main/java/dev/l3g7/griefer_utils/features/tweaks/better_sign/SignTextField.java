package dev.l3g7.griefer_utils.features.tweaks.better_sign;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.MathHelper;

import static org.lwjgl.input.Keyboard.*;

/**
 * Copied and modified from net.minecraft.client.gui.GuiTextField
 * TODO: beautify (ASM?)
 */
public class SignTextField extends Gui {

	private final int id;
	private final FontRenderer font;
	public int xPos;
	public int yPos;
	public int width;
	public int height;
	private String text = "";
	private int cursorCounter;
	private boolean isFocused;
	private boolean isEnabled = true;
	private int cursorPos;
	private int selectionEnd;

	public SignTextField(int componentId, FontRenderer fontRendererObj, int x, int y, int par5Width, int par6Height) {
		id = componentId;
		font = fontRendererObj;
		xPos = x;
		yPos = y;
		width = par5Width;
		height = par6Height;
	}

	public void updateCursorCounter() {
		++cursorCounter;
	}

	public void setText(String p_146180_1_) {
		text = p_146180_1_;

		setCursorPositionEnd();
	}

	public String getText() {
		return text;
	}

	public String getSelectedText() {
		int i = Math.min(cursorPos, selectionEnd);
		int j = Math.max(cursorPos, selectionEnd);
		return text.substring(i, j);
	}

	public void writeText(String text) {
		String str = "";
		String in = ChatAllowedCharacters.filterAllowedCharacters(text);
		int selStart = Math.min(cursorPos, selectionEnd);
		int selEnd = Math.max(cursorPos, selectionEnd);
		int length;

		if (this.text.length() > 0)
			str = str + this.text.substring(0, selStart);

		str = str + in;
		length = in.length();

		if (this.text.length() > 0 && selEnd < this.text.length())
			str = str + this.text.substring(selEnd);

		if (font.getStringWidth(str) > 90) {
			if (in.length() == 1)
				return;

			writeText(in.substring(0, in.length() - 1));
			return;
		}
		this.text = str;
		moveCursorBy(selStart - selectionEnd + length);
	}

	public void deleteWords(int p_146177_1_) {
		if (text.length() == 0)
			return;

		if (selectionEnd != cursorPos)
			writeText("");
		else
			deleteFromCursor(getNthWordFromCursor(p_146177_1_) - cursorPos);
	}

	public void deleteFromCursor(int p_146175_1_) {
		if (text.length() == 0)
			return;

		if (selectionEnd != cursorPos) {
			writeText("");
			return;
		}

		boolean flag = p_146175_1_ < 0;
		int i = flag ? cursorPos + p_146175_1_ : cursorPos;
		int j = flag ? cursorPos : cursorPos + p_146175_1_;
		String s = "";

		if (i >= 0) {
			s = text.substring(0, i);
		}

		if (j < text.length()) {
			s = s + text.substring(j);
		}
		text = s;

		if (flag) {
			moveCursorBy(p_146175_1_);
		}
	}

	public int getId() {
		return id;
	}

	public int getNthWordFromCursor(int p_146187_1_) {
		return getNthWordFromPos(p_146187_1_, getCursorPosition());
	}

	public int getNthWordFromPos(int p_146183_1_, int p_146183_2_) {
		return func_146197_a(p_146183_1_, p_146183_2_, true);
	}

	public int func_146197_a(int p_146197_1_, int p_146197_2_, boolean p_146197_3_) {
		int i = p_146197_2_;
		boolean flag = p_146197_1_ < 0;
		int j = Math.abs(p_146197_1_);

		for (int k = 0; k < j; ++k) {
			if (!flag) {
				int l = text.length();
				i = text.indexOf(32, i);

				if (i == -1) {
					i = l;
				} else {
					while (p_146197_3_ && i < l && text.charAt(i) == 32) {
						++i;
					}
				}
			} else {
				while (p_146197_3_ && i > 0 && text.charAt(i - 1) == 32) {
					--i;
				}

				while (i > 0 && text.charAt(i - 1) != 32) {
					--i;
				}
			}
		}

		return i;
	}

	public void moveCursorBy(int p_146182_1_) {
		setCursorPosition(selectionEnd + p_146182_1_);
	}

	public void setCursorPosition(int p_146190_1_) {
		cursorPos = p_146190_1_;
		int i = text.length();
		cursorPos = MathHelper.clamp_int(cursorPos, 0, i);
		setSelectionPos(cursorPos);
	}

	public void setCursorPositionZero() {
		setCursorPosition(0);
	}

	public void setCursorPositionEnd() {
		setCursorPosition(text.length());
	}

	private boolean isEverythingSelected() {
		return cursorPos == text.length() && selectionEnd == 0;
	}

	public boolean textBoxKeyTyped(char typedChar, int keyCode) {
		if (!isFocused)
			return false;

		if (GuiScreen.isKeyComboCtrlA(keyCode)) {

			// Return false if everything is already selected to tell BetterGuiEditSign to select the other SignTextFields as well
			if (isEverythingSelected())
				return false;

			setCursorPositionEnd();
			setSelectionPos(0);
			return true;
		}

		switch (keyCode) {
		case KEY_BACK:
			if (!isEnabled)
				return true;

			if (GuiScreen.isCtrlKeyDown())
				deleteWords(-1);
			else
				deleteFromCursor(-1);

			return true;

		case KEY_HOME:

			if (GuiScreen.isShiftKeyDown())
				setSelectionPos(0);
			else
				setCursorPositionZero();

			return true;

		case KEY_LEFT:

			if (GuiScreen.isShiftKeyDown()) {
				if (GuiScreen.isCtrlKeyDown()) {
					setSelectionPos(getNthWordFromPos(-1, getSelectionEnd()));
				} else {
					setSelectionPos(getSelectionEnd() - 1);
				}
			} else if (GuiScreen.isCtrlKeyDown()) {
				setCursorPosition(getNthWordFromCursor(-1));
			} else {
				moveCursorBy(-1);
			}

			return true;

		case KEY_RIGHT:

			if (GuiScreen.isShiftKeyDown()) {
				if (GuiScreen.isCtrlKeyDown()) {
					setSelectionPos(getNthWordFromPos(1, getSelectionEnd()));
				} else {
					setSelectionPos(getSelectionEnd() + 1);
				}
			} else if (GuiScreen.isCtrlKeyDown()) {
				setCursorPosition(getNthWordFromCursor(1));
			} else {
				moveCursorBy(1);
			}

			return true;

		case KEY_END:

			if (GuiScreen.isShiftKeyDown())
				setSelectionPos(text.length());
			else
				setCursorPositionEnd();

			return true;

		case KEY_DELETE:
			if (!isEnabled)
				return true;

			if (GuiScreen.isCtrlKeyDown())
				deleteWords(1);
			else
				deleteFromCursor(1);

			return true;

		default:
			boolean isAllowed = ChatAllowedCharacters.isAllowedCharacter(typedChar);

			if (isAllowed && isEnabled)
				writeText(Character.toString(typedChar));

			return isAllowed;
		}
	}

	public void mouseClicked(int mouseX, int mouseY, int mouseButton, int width) {
		int center = width / 2;
		boolean inArea = mouseX >= center - 47 && mouseX < center + 47 && mouseY >= 100 + yPos
				&& mouseY < 100 + yPos + 10;

		if (inArea && mouseButton == 0)
			setCursorPosition(font
					.trimStringToWidth(font.trimStringToWidth(text, mouseX - center - xPos),
							mouseX - xPos)
					.length());
	}

	public void drawTextBox() {
		int selEnd = Math.min(selectionEnd, text.length());
		boolean cursorValid = cursorPos >= 0 && cursorPos <= text.length();
		boolean showCursor = isFocused && cursorCounter / 6 % 2 == 0 && cursorValid;
		int xPos2 = xPos;

		if (text.length() > 0) {
			String s1 = cursorValid ? text.substring(0, cursorPos) : text;
			xPos2 = font.drawString(s1, xPos, yPos, 0);
		}

		boolean atLimit = cursorPos < text.length();
		int xPos3 = xPos2;

		if (!cursorValid) {
			xPos3 = cursorPos > 0 ? xPos + width : xPos;
		} else if (atLimit) {
			xPos3 = xPos2 - 1;
			--xPos2;
		}

		if (text.length() > 0 && cursorValid && cursorPos < text.length()) {
			font.drawString(text.substring(cursorPos), xPos2 + 1, yPos, 0);
		}

		if (showCursor) {
			if (atLimit) {
				Gui.drawRect(xPos3, yPos - 1, xPos3 + 1, yPos + 1 + font.FONT_HEIGHT, 0xFF000000);
			} else {
				font.drawString("> ", xPos - font.getStringWidth("> "), yPos, 0);
				font.drawString(" <", xPos3, yPos, 0);
			}
		}

		if (selEnd != cursorPos) {
			int l1 = xPos + font.getStringWidth(text.substring(0, selEnd));
			drawCursorVertical(xPos3, yPos - 1, l1 - 1, yPos + 1 + font.FONT_HEIGHT);
		}
	}

	private void drawCursorVertical(int p_146188_1_, int p_146188_2_, int p_146188_3_, int p_146188_4_) {
		if (p_146188_1_ < p_146188_3_) {
			int i = p_146188_1_;
			p_146188_1_ = p_146188_3_;
			p_146188_3_ = i;
		}

		if (p_146188_2_ < p_146188_4_) {
			int j = p_146188_2_;
			p_146188_2_ = p_146188_4_;
			p_146188_4_ = j;
		}

		if (p_146188_3_ > xPos + width) {
			p_146188_3_ = xPos + width;
		}

		if (p_146188_1_ > xPos + width) {
			p_146188_1_ = xPos + width;
		}

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
		GlStateManager.disableTexture2D();
		GlStateManager.enableColorLogic();
		GlStateManager.colorLogicOp(5387);
		worldrenderer.begin(7, DefaultVertexFormats.POSITION);
		worldrenderer.pos(p_146188_1_, p_146188_4_, 0.0D).endVertex();
		worldrenderer.pos(p_146188_3_, p_146188_4_, 0.0D).endVertex();
		worldrenderer.pos(p_146188_3_, p_146188_2_, 0.0D).endVertex();
		worldrenderer.pos(p_146188_1_, p_146188_2_, 0.0D).endVertex();
		tessellator.draw();
		GlStateManager.disableColorLogic();
		GlStateManager.enableTexture2D();
	}

	public int getCursorPosition() {
		return cursorPos;
	}

	public void setFocused(boolean focused) {
		if (focused && !isFocused)
			cursorCounter = 0;

		isFocused = focused;
	}

	public void setEnabled(boolean p_146184_1_) {
		isEnabled = p_146184_1_;
	}

	public int getSelectionEnd() {
		return selectionEnd;
	}

	public void setSelectionPos(int pos) {
		selectionEnd = MathHelper.clamp_int(pos, 0, text.length());
	}

}