package dev.l3g7.griefer_utils.v1_8_9.bridges;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.api.bridges.Bridge;
import dev.l3g7.griefer_utils.api.bridges.MinecraftBridge.McItemStack;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.util.Util;
import dev.l3g7.griefer_utils.laby4.bridges.ItemBridge;
import net.labymod.api.client.world.item.ItemStack;
import net.labymod.v1_8_9.client.util.ItemUtil;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;

import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;

@Bridge
@Singleton
@SuppressWarnings("UnstableApiUsage")
public class ItemBridgeImpl implements ItemBridge {

	@Override
	public McItemStack getDefaultStack() {
		return c(new net.minecraft.item.ItemStack(Blocks.stone, 1, 10000));
	}

	@Override
	public ItemStack toLabyStack(McItemStack nbt) {
		return ItemUtil.getLabyItemStack(c(nbt));
	}

	@Override
	public McItemStack fromLabyStack(ItemStack itemStack) {
		return c(ItemUtil.getMinecraftItemStack(itemStack));
	}

	@Override
	public JsonElement serialize(McItemStack itemStack) {
		if (itemStack == null)
			return JsonNull.INSTANCE;

		//noinspection DataFlowIssue
		return new JsonPrimitive(((net.minecraft.item.ItemStack) c(itemStack)).writeToNBT(new NBTTagCompound()).toString());
	}

	@Override
	public McItemStack deserialize(JsonElement nbt) {
		if (nbt.isJsonNull())
			return null;

		try {
			return c(net.minecraft.item.ItemStack.loadItemStackFromNBT(JsonToNBT.getTagFromJson(nbt.getAsString())));
		} catch (NBTException e) {
			throw Util.elevate(e);
		}
	}

	@Override
	public boolean isConvertableToLabyStack(Object obj) {
		return obj instanceof McItemStack || obj instanceof Item || obj instanceof Block;
	}

	@Override
	public ItemStack convertToLabyStack(Object obj) {
		if (obj instanceof McItemStack itemStack)
			return toLabyStack(itemStack);

		if (obj instanceof Item item)
			return toLabyStack(c(new net.minecraft.item.ItemStack(item)));

		if (obj instanceof Block block)
			return toLabyStack(c(new net.minecraft.item.ItemStack(block)));

		throw new IllegalArgumentException("Can't convert " + obj + " to LabyMod's ItemStack!");
	}

	@Mixin(net.minecraft.item.ItemStack.class)
	public static abstract class MixinItemStack implements McItemStack {}

}
