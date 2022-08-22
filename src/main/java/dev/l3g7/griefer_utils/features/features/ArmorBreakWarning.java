package dev.l3g7.griefer_utils.features.features;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import dev.l3g7.griefer_utils.util.RenderUtil;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Singleton
public class ArmorBreakWarning extends Feature {

	private static final char[] chars = new char[] {'\u2502', '\u2593', '\u2592', '\u2591'};

	private Character currentChar = null;
	private long displayEndTime = 0;
	private final boolean[] warnedAbout = new boolean[4];

	private final NumberSetting damage = new NumberSetting()
			.name("ArmorBreakWarning")
			.description("Zeigt eine Warnung an, sobald eine angezogene Rüstung die eingestellte Haltbarkeit unterschreitet.\n" +
					"(0 zum Deaktivieren)")
			.config("features.armor_break_warning.active")
			.defaultValue(0)
			.icon(Material.DIAMOND_CHESTPLATE);

	public ArmorBreakWarning() {
		super(Category.FEATURE);
	}

	@Override
	public SettingsElement getMainElement() {
		return damage;
	}

	@SubscribeEvent
	public void onTick(TickEvent.PlayerTickEvent event) {
		if (!isCategoryEnabled()
				|| damage.get() == 0
				|| player() == null)
			return;

		for (int i = 0; i < player().inventory.armorInventory.length; i++) {
			ItemStack stack = player().inventory.armorInventory[i];

			if (stack != null && stack.isItemStackDamageable() && damage.get() > stack.getMaxDamage() - stack.getItemDamage()) {
				if (!warnedAbout[i]) {
					displayEndTime = System.currentTimeMillis() + 6000;
					currentChar = chars[i];
					warnedAbout[i] = true;
				}
				continue;
			}

			warnedAbout[i] = false;
		}
	}

	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event) {
		if (!getCategory().setting.get()
				|| damage.get() == 0
				|| currentChar == null
				|| displayEndTime <= System.currentTimeMillis())
			return;

		if (mc().currentScreen != null && !(mc().currentScreen instanceof GuiChat))
			return;

		ScaledResolution res = new ScaledResolution(mc());
		renderSubTitle(String.format("§c%c geht kaputt!", currentChar), res.getScaledWidth(), res.getScaledHeight());
	}

	/**
	 * Based on {@link net.minecraft.client.gui.GuiIngame#renderGameOverlay(float)}}
	 */
	private void renderSubTitle(String title, int width, int height) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(width / 2f, height / 2f, 0);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.pushMatrix();
		GlStateManager.scale(2.0F, 2.0F, 2.0F);
		RenderUtil.symbolFontRenderer.drawString(title, (float)(-RenderUtil.symbolFontRenderer.getStringWidth(title) / 2), 5.0F, 0xFFFFFF, true);
		GlStateManager.popMatrix();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

}
