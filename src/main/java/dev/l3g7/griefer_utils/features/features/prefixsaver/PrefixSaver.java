package dev.l3g7.griefer_utils.features.features.prefixsaver;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.ItemBuilder;
import dev.l3g7.griefer_utils.misc.TickScheduler;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.init.Blocks;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Singleton
public class PrefixSaver extends Feature {

	private final BooleanSetting enabled = new BooleanSetting()
	 .name("PrefixSaver")
	 .config("features.prefix_saver.active")
	 .icon(new ItemBuilder(Blocks.redstone_ore).enchant())
	 .defaultValue(false);

	public PrefixSaver() {
		super(Category.FEATURE);
	}

	@Override
	public SettingsElement getMainElement() {
		return enabled;
	}

	@SubscribeEvent
	public void onMouseClick(MouseEvent event) {
		if (!isActive() || !isOnGrieferGames())
			return;

		if (event.button != 1 || !event.buttonstate)
			return;

		if (!"§fVergibt §aein Farbrecht§f! (Rechtsklick)".equals(ItemUtil.getLastLore(mc().thePlayer.getHeldItem())))
			return;

		TickScheduler.runNextTick(() -> mc().displayGuiScreen(new GuiPrefixSaver(mc())));
		event.setCanceled(true);
	}
}
