package dev.l3g7.griefer_utils.features.modules;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.features.Module;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.Material;
import net.minecraft.nbt.NBTTagCompound;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

@Singleton
public class MissingAdventurerBlocks extends Module {

	public MissingAdventurerBlocks() {
		super("Fehlende Adv. Blöcke", "Zeigt dir an, wie viele Blöcke mit dem in der Hand gehaltenen Adventure-Werkzeug noch abgebaut werden müssen.", "missing_adventurer_blocks", new ControlElement.IconData(Material.FIREBALL));
	}

	@Override
	public boolean isShown() {
		return super.isShown() && getMissingBlocks() != -1;
	}

	@Override
	public String[] getKeys() {
		return getDefaultKeys();
	}

	@Override
	public String[] getDefaultKeys() {
		return new String[] { "Fehlende Blöcke" };
	}

	@Override
	public String[] getValues() {
		return new String[] { Constants.DECIMAL_FORMAT_98.format(getMissingBlocks()) };
	}

	@Override
	public String[] getDefaultValues() {
		return new String[] { "0" };
	}

	private int getMissingBlocks() {
		if (player() == null || player().getHeldItem() == null)
			return -1;

		NBTTagCompound tag = player().getHeldItem().getTagCompound();
		if (tag == null || !tag.hasKey("adventure"))
			return -1;

		NBTTagCompound adventureTag = tag.getCompoundTag("adventure");
		return adventureTag.getInteger("adventure.req_amount") - adventureTag.getInteger("adventure.amount");

	}

}
