package dev.l3g7.griefer_utils.features.features.prefixsaver;

import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.misc.TickScheduler;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Singleton
public class PrefixSaver extends Feature {

	private final BooleanSetting enabled = new BooleanSetting()
	 .name("PrefixSaver")
	 .config("features.prefix_saver.active")
	 .icon(Material.COMPASS)
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

		if (!isHoldingPrefix())
			return;

		TickScheduler.runNextTick(() -> {
			mc().displayGuiScreen(new GuiPrefixSaver(mc()));
		});
		event.setCanceled(true);

	}

	private boolean isHoldingPrefix() {
		ItemStack itemStack = mc().thePlayer.getHeldItem();
		if (itemStack == null)
			return false;

		NBTTagCompound tag = itemStack.getTagCompound();
		if (tag == null || !tag.hasKey("display", 10))
			return false;

		NBTTagCompound nbt = tag.getCompoundTag("display");
		if (nbt.getTagId("Lore") != 9)
			return false;

		NBTTagList lore = nbt.getTagList("Lore", 8);
		if (lore.tagCount() < 1)
			return false;

		return lore.getStringTagAt(lore.tagCount() - 1).equals("\u00a7fVergibt \u00a7aein Farbrecht\u00a7f! (Rechtsklick)");
	}
}
