package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;

import java.util.function.BiFunction;

/**
 * Backport of 1.13.2 GuiTextField to 1.8.9.
 */
@SuppressWarnings("JavadocReference")
public class TextFieldShim extends GuiTextField {

	private final FontRenderer sFontRenderer;
	private final int sWidth;
	private final int sHeight;
	private int sCursorCounter;
	private boolean sIsEnabled = true;
	private int sLineScrollOffset;
	private int sCursorPosition;
	private int sEnabledColor = 0xE0E0E0;
	private int sDisabledColor = 0x707070;

	private Runnable guiResponder;
	private BiFunction<String, Integer, String> textFormatter;
	private String suggestion;

	public TextFieldShim(int id, FontRenderer fontRenderer, int x, int y, int width, int height) {
		super(id, fontRenderer, x, y, width, height);
		this.sFontRenderer = fontRenderer;
		this.sWidth = width;
		this.sHeight = height;
		this.textFormatter = (p_195610_0_, p_195610_1_) -> p_195610_0_;
	}

	/**
	 * Shim to get access to {@link GuiTextField#cursorCounter}.
	 */
	@Override
	public void updateCursorCounter() {
		super.updateCursorCounter();
		sCursorCounter++;
	}

	/**
	 * Shim to get access to {@link GuiTextField#cursorCounter}.
	 */
	@Override
	public void setFocused(boolean focus) {
		if (focus && !this.isFocused())
			this.sCursorCounter = 0;

		super.setFocused(focus);
	}

	/**
	 * Shim to get access to {@link GuiTextField#isEnabled}.
	 */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		this.sIsEnabled = enabled;
	}

	/**
	 * Shim to get access to {@link GuiTextField#enabledColor}.
	 */
	@Override
	public void setTextColor(int color) {
		super.setTextColor(color);
		this.sEnabledColor = color;
	}

	/**
	 * Shim to get access to {@link GuiTextField#disabledColor}.
	 */
	@Override
	public void setDisabledTextColour(int color) {
		super.setDisabledTextColour(color);
		this.sDisabledColor = color;
	}

	/**
	 * Shim to get access to {@link GuiTextField#cursorPosition}.
	 */
	@Override
	public void setCursorPosition(int pos) {
		super.setCursorPosition(pos);
		int newPos = MathHelper.clamp_int(pos, 0, getText().length());
		if (this.sCursorPosition != newPos)
			runResponder();

		this.sCursorPosition = newPos;
	}

	/**
	 * Shim to get access to {@link GuiTextField#lineScrollOffset}.
	 */
	@Override
	public void setSelectionPos(int pos) {
		super.setSelectionPos(pos);
		int length = getText().length();
		pos = MathHelper.clamp_int(pos, 0, length);

		if (sFontRenderer == null)
			return;

		if (sLineScrollOffset > length)
			sLineScrollOffset = length;

		String text = sFontRenderer.trimStringToWidth(getText().substring(sLineScrollOffset), getWidth());
		int absOffset = text.length() + sLineScrollOffset;
		if (pos == sLineScrollOffset)
			sLineScrollOffset -= sFontRenderer.trimStringToWidth(getText(), getWidth(), true).length();

		if (pos > absOffset)
			sLineScrollOffset += pos - absOffset;
		else if (pos <= sLineScrollOffset)
			sLineScrollOffset -= sLineScrollOffset - pos;

		sLineScrollOffset = MathHelper.clamp_int(sLineScrollOffset, 0, length);
	}

	@Override
	public void setMaxStringLength(int maxStringLength) {
		int previousMaxLength = getMaxStringLength();
		super.setMaxStringLength(maxStringLength);

		if (previousMaxLength != maxStringLength)
			runResponder();
	}

	/**
	 * Disables the responder while setting the text.
	 */
	@Override
	public void setText(String lvt_1_1_) {
		Runnable responder = this.guiResponder;
		this.guiResponder = null;
		super.setText(lvt_1_1_);
		this.guiResponder = responder;
	}

	@Override
	public void writeText(String lvt_1_1_) {
		super.writeText(lvt_1_1_);
		runResponder();
	}

	@Override
	public void deleteFromCursor(int lvt_1_1_) {
		super.deleteFromCursor(lvt_1_1_);
		runResponder();
	}

	public void runResponder() {
		if (this.guiResponder != null)
			this.guiResponder.run();
	}

	public void setSuggestion(String suggestion) {
		this.suggestion = suggestion;
	}

	public void setTextAcceptHandler(Runnable p_195609_1_) {
		this.guiResponder = p_195609_1_;
	}

	public void setTextFormatter(BiFunction<String, Integer, String> p_195607_1_) {
		this.textFormatter = p_195607_1_;
	}

	@Override
	public void drawTextBox() {
		if (!this.getVisible())
			return;

		if (this.getEnableBackgroundDrawing()) {
			drawRect(this.xPosition - 1, this.yPosition - 1, this.xPosition + sWidth + 1, this.yPosition + this.sHeight + 1, -6250336);
			drawRect(this.xPosition, this.yPosition, this.xPosition + sWidth, this.yPosition + this.sHeight, -16777216);
		}

		int color = this.sIsEnabled ? this.sEnabledColor : this.sDisabledColor;
		int cursorPos = this.sCursorPosition - this.sLineScrollOffset;
		int selectionEnd = this.getSelectionEnd() - this.sLineScrollOffset;
		String shownText = this.sFontRenderer.trimStringToWidth(this.getText().substring(this.sLineScrollOffset), this.getWidth());
		boolean inShownText = cursorPos >= 0 && cursorPos <= shownText.length();
		boolean showCursor = this.isFocused() && this.sCursorCounter / 6 % 2 == 0 && inShownText;
		int cursorX = this.getEnableBackgroundDrawing() ? this.xPosition + 4 : this.xPosition;
		int cursorY = this.getEnableBackgroundDrawing() ? this.yPosition + (this.sHeight - 8) / 2 : this.yPosition;
		int cursorX2 = cursorX;
		if (selectionEnd > shownText.length())
			selectionEnd = shownText.length();

		if (!shownText.isEmpty()) {
			String text = inShownText ? shownText.substring(0, cursorPos) : shownText;
			cursorX2 = this.sFontRenderer.drawStringWithShadow(this.textFormatter.apply(text, this.sLineScrollOffset), (float) cursorX, (float) cursorY, color);
		}

		boolean inText = this.sCursorPosition < this.getText().length() || this.getText().length() >= this.getMaxStringLength();
		int cursorX3 = cursorX2;
		if (!inShownText) {
			cursorX3 = cursorPos > 0 ? cursorX + sWidth : cursorX;
		} else if (inText) {
			cursorX3 = cursorX2 - 1;
			--cursorX2;
		}

		if (!shownText.isEmpty() && inShownText && cursorPos < shownText.length())
			this.sFontRenderer.drawStringWithShadow(this.textFormatter.apply(shownText.substring(cursorPos), this.sCursorPosition), (float) cursorX2, (float) cursorY, color);

		if (!inText && this.suggestion != null)
			this.sFontRenderer.drawStringWithShadow(this.suggestion, (float) (cursorX3 - 1), (float) cursorY, -8355712);

		if (showCursor) {
			if (inText)
				Gui.drawRect(cursorX3, cursorY - 1, cursorX3 + 1, cursorY + 1 + this.sFontRenderer.FONT_HEIGHT, -3092272);
			else
				this.sFontRenderer.drawStringWithShadow("_", (float) cursorX3, (float) cursorY, color);
		}

		if (selectionEnd != cursorPos) {
			int textWidth = cursorX + this.sFontRenderer.getStringWidth(shownText.substring(0, selectionEnd));
			drawCursorVertical(cursorX3, cursorY - 1, textWidth - 1, cursorY + 1 + this.sFontRenderer.FONT_HEIGHT);
		}
	}

	private void drawCursorVertical(int x1, int y1, int x2, int y2) {
		if (x1 < x2) {
			int lvt_5_1_ = x1;
			x1 = x2;
			x2 = lvt_5_1_;
		}

		if (y1 < y2) {
			int lvt_5_2_ = y1;
			y1 = y2;
			y2 = lvt_5_2_;
		}

		if (x2 > this.xPosition + this.sWidth) {
			x2 = this.xPosition + this.sWidth;
		}

		if (x1 > this.xPosition + this.sWidth) {
			x1 = this.xPosition + this.sWidth;
		}

		Tessellator lvt_5_3_ = Tessellator.getInstance();
		WorldRenderer lvt_6_1_ = lvt_5_3_.getWorldRenderer();
		GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
		GlStateManager.disableTexture2D();
		GlStateManager.enableColorLogic();
		GlStateManager.colorLogicOp(5387);
		lvt_6_1_.begin(7, DefaultVertexFormats.POSITION);
		lvt_6_1_.pos(x1, y2, 0.0F).endVertex();
		lvt_6_1_.pos(x2, y2, 0.0F).endVertex();
		lvt_6_1_.pos(x2, y1, 0.0F).endVertex();
		lvt_6_1_.pos(x1, y1, 0.0F).endVertex();
		lvt_5_3_.draw();
		GlStateManager.disableColorLogic();
		GlStateManager.enableTexture2D();
	}

}
