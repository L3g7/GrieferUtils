package dev.l3g7.griefer_utils.features.tweaks.better_sign;

import dev.l3g7.griefer_utils.util.Reflection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Arrays;


/**
 * Copied and modified from net.minecraft.client.gui.inventory.GuiEditSign
 * TODO: beautify (ASM?)
 */
public class BetterGuiEditSign extends GuiScreen {

    private static final ModelSign model = new ModelSign();

    private static final ResourceLocation SIGN_TEXTURE = new ResourceLocation("textures/entity/sign.png");

    private final TileEntitySign tileSign;
    private int updateCounter;
    private int editLine;
    private GuiButton doneBtn;
    private final SignTextField[] lines = new SignTextField[4];

    public BetterGuiEditSign(GuiEditSign gui) {
        tileSign = Reflection.get(gui, "tileSign", "field_146848_f", "a");
    }

    @Override
    public void initGui() {
        buttonList.clear();
        Keyboard.enableRepeatEvents(true);
        buttonList.add(doneBtn = new GuiButton(0, width / 2 - 100, height / 4 + 120, I18n.format("gui.done")));
        tileSign.setEditable(false);
        for (int i = 0; i < 4; i++) {
            lines[i] = new SignTextField(-1, Minecraft.getMinecraft().fontRendererObj, 0, 2 + 10 * i, 200, 10);
            lines[i].setText(tileSign.signText[i].getUnformattedText());
        }
        lines[0].setFocused(true);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        NetHandlerPlayClient nethandlerplayclient = mc.getNetHandler();

        if (nethandlerplayclient != null)
            nethandlerplayclient.addToSendQueue(new C12PacketUpdateSign(tileSign.getPos(), tileSign.signText));

        tileSign.setEditable(true);
    }

    @Override
    public void updateScreen() {
        ++updateCounter;
        Arrays.stream(lines).forEach(SignTextField::updateCursorCounter);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button.id == 0) {
                tileSign.markDirty();
                mc.displayGuiScreen(null);
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (lines[editLine].textBoxKeyTyped(typedChar, keyCode)) {
            tileSign.signText[editLine] = new ChatComponentText(lines[editLine].getText());
        } else {
            super.keyTyped(typedChar, keyCode);

            if (keyCode == 200) {
                editLine = editLine - 1 & 3;
                lines[editLine].setCursorPositionEnd();
            }

            if (keyCode == 208 || keyCode == 28 || keyCode == 156) {
                editLine = editLine + 1 & 3;
                lines[editLine].setCursorPositionEnd();
            }

            String s = tileSign.signText[editLine].getUnformattedText();

            if (keyCode == 14 && s.length() > 0) {
                s = s.substring(0, s.length() - 1);
            }

            if (ChatAllowedCharacters.isAllowedCharacter(typedChar)
                    && fontRendererObj.getStringWidth(s + typedChar) <= 90) {
                s = s + typedChar;
            }

            tileSign.signText[editLine] = new ChatComponentText(s);

            if (keyCode == 1) {
                actionPerformed(doneBtn);
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        editLine = MathHelper.clamp_int((mouseY - 100) / 10, 0, 3);
        lines[editLine].mouseClicked(mouseX, mouseY, mouseButton, width);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, I18n.format("sign.edit"), width / 2, 40, 0xFFFFFF);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) (width / 2), 0.0F, 50.0F);
        float f = 93.75F;
        GlStateManager.scale(-f, -f, -f);

        GlStateManager.translate(0.0F, -1.0625F, 0.0F);

        if (updateCounter / 6 % 2 == 0)
            tileSign.lineBeingEdited = editLine;


        GlStateManager.pushMatrix();

        float f3 = 0.6666667F;

        GlStateManager.translate((float) 0, (float) -0.75 + 0.75F * f3, 0);
        GlStateManager.rotate(-180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0F, -0.3125F, -0.4375F);
        model.signStick.showModel = false;

        Minecraft.getMinecraft().getTextureManager().bindTexture(SIGN_TEXTURE);

        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();
        GlStateManager.scale(f3, -f3, -f3);
        model.renderSign();
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();


        tileSign.lineBeingEdited = -1;

        GlStateManager.scale(1 / -f, 1 / -f, 1 / -f);
        int i = 0;
        for (SignTextField line : lines) {
            int w = fontRendererObj.getStringWidth(line.getText());
            line.xPos = -w / 2;
            line.drawTextBox();
            line.setFocused(editLine == i++);
        }
        GlStateManager.scale(-f, -f, -f);

        GlStateManager.popMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

}